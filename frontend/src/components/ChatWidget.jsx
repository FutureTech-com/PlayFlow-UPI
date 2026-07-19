import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { aiApi } from '../api/ai'
import { MessageCircle, X, Send, Mic, MicOff, Sparkles, Volume2 } from 'lucide-react'

const SpeechRecognitionCtor =
  typeof window !== 'undefined' ? window.SpeechRecognition || window.webkitSpeechRecognition : null

/**
 * Floating support chatbot, also doubling as the "Voice Assistant" (Hello PayFlow) feature:
 * the mic button transcribes speech with the Web Speech API, sends the transcript through
 * the same /api/ai/chat endpoint as typed messages, and speaks the reply back with
 * speechSynthesis. One backend intent-matcher, two input modes.
 */
export default function ChatWidget() {
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState([
    { role: 'bot', text: "Hi! I'm the PayFlow assistant. Ask me about your balance, UPI ID, recent payments, or tap the mic to talk to me.", actions: [] },
  ])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [listening, setListening] = useState(false)
  const [voiceSupported] = useState(!!SpeechRecognitionCtor)
  const recognitionRef = useRef(null)
  const scrollRef = useRef(null)
  const navigate = useNavigate()

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages, open])

  const speak = (text) => {
    if (!window.speechSynthesis) return
    window.speechSynthesis.cancel()
    const utterance = new SpeechSynthesisUtterance(text)
    utterance.rate = 1.02
    window.speechSynthesis.speak(utterance)
  }

  const sendMessage = async (text, { viaVoice = false } = {}) => {
    const trimmed = text.trim()
    if (!trimmed) return
    setMessages((m) => [...m, { role: 'user', text: trimmed, actions: [] }])
    setInput('')
    setSending(true)
    try {
      const res = await aiApi.chat(trimmed)
      setMessages((m) => [...m, { role: 'bot', text: res.reply, actions: res.suggestedActions || [] }])
      if (viaVoice) speak(res.reply)
    } catch {
      const fallback = "Sorry, I couldn't reach the assistant just now. Try again in a moment."
      setMessages((m) => [...m, { role: 'bot', text: fallback, actions: [] }])
      if (viaVoice) speak(fallback)
    } finally {
      setSending(false)
    }
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    sendMessage(input)
  }

  const startListening = () => {
    if (!SpeechRecognitionCtor) return
    const recognition = new SpeechRecognitionCtor()
    recognition.lang = 'en-IN'
    recognition.interimResults = false
    recognition.maxAlternatives = 1

    recognition.onstart = () => setListening(true)
    recognition.onerror = () => setListening(false)
    recognition.onend = () => setListening(false)
    recognition.onresult = (event) => {
      const transcript = event.results[0][0].transcript
      sendMessage(transcript, { viaVoice: true })
    }

    recognitionRef.current = recognition
    recognition.start()
  }

  const stopListening = () => {
    recognitionRef.current?.stop()
    setListening(false)
  }

  const handleAction = (action) => {
    setOpen(false)
    navigate(action.route)
  }

  return (
    <>
      {/* Floating launcher button */}
      <button
        onClick={() => setOpen((v) => !v)}
        className="fixed z-40 bottom-20 sm:bottom-6 right-4 sm:right-6 w-14 h-14 rounded-full bg-[var(--color-primary)] text-white shadow-lg flex items-center justify-center hover:bg-[var(--color-primary-dark)] transition-colors"
        aria-label="PayFlow assistant"
      >
        {open ? <X size={22} /> : <MessageCircle size={22} />}
      </button>

      {open && (
        <div className="fixed z-40 bottom-36 sm:bottom-24 right-4 sm:right-6 w-[calc(100%-2rem)] max-w-sm h-[28rem] bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl shadow-2xl flex flex-col overflow-hidden">
          <div className="flex items-center gap-2 px-4 py-3 bg-[var(--color-ink)] text-white">
            <Sparkles size={16} className="text-[var(--color-accent)]" />
            <p className="text-sm font-semibold flex-1">PayFlow Assistant</p>
            {voiceSupported && <span className="text-[10px] text-white/50 flex items-center gap-1"><Volume2 size={11} /> Voice enabled</span>}
          </div>

          <div ref={scrollRef} className="flex-1 overflow-y-auto px-4 py-3 space-y-3">
            {messages.map((m, i) => (
              <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[85%] ${m.role === 'user' ? '' : ''}`}>
                  <div
                    className={`text-sm rounded-2xl px-3 py-2 ${
                      m.role === 'user'
                        ? 'bg-[var(--color-primary)] text-white rounded-br-sm'
                        : 'bg-[var(--color-paper)] text-[var(--color-ink)] rounded-bl-sm'
                    }`}
                  >
                    {m.text}
                  </div>
                  {m.actions?.length > 0 && (
                    <div className="flex flex-wrap gap-1.5 mt-1.5">
                      {m.actions.map((a) => (
                        <button
                          key={a.route}
                          onClick={() => handleAction(a)}
                          className="text-xs font-medium rounded-full border border-[var(--color-primary)] text-[var(--color-primary)] px-2.5 py-1 hover:bg-[var(--color-primary)] hover:text-white transition-colors"
                        >
                          {a.label}
                        </button>
                      ))}
                    </div>
                  )}
                </div>
              </div>
            ))}
            {sending && <div className="text-xs text-[var(--color-muted)]">Assistant is typing…</div>}
          </div>

          <form onSubmit={handleSubmit} className="flex items-center gap-2 p-3 border-t border-[var(--color-line)]">
            {voiceSupported && (
              <button
                type="button"
                onClick={listening ? stopListening : startListening}
                className={`shrink-0 w-9 h-9 rounded-full flex items-center justify-center transition-colors ${
                  listening ? 'bg-[var(--color-danger)] text-white animate-pulse' : 'bg-[var(--color-paper)] text-[var(--color-primary)]'
                }`}
                aria-label={listening ? 'Stop listening' : 'Speak to PayFlow assistant'}
              >
                {listening ? <MicOff size={16} /> : <Mic size={16} />}
              </button>
            )}
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder={listening ? 'Listening…' : 'Ask me anything…'}
              disabled={listening}
              className="flex-1 rounded-full border border-[var(--color-line)] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/30"
            />
            <button
              type="submit"
              disabled={sending || !input.trim()}
              className="shrink-0 w-9 h-9 rounded-full bg-[var(--color-primary)] text-white flex items-center justify-center disabled:opacity-50"
            >
              <Send size={15} />
            </button>
          </form>
        </div>
      )}
    </>
  )
}
