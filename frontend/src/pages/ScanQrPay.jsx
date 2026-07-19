import { useEffect, useRef, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import jsQR from 'jsqr'
import { ArrowLeft, Camera, ImageUp, ZapOff, AlertCircle } from 'lucide-react'

/**
 * Parses the "upi://pay?pa=...&pn=...&am=...&tn=..." deep-link format that
 * QrCodeService.java encodes (see backend/.../service/QrCodeService.java).
 * Falls back to treating the raw scanned text as a bare VPA if it doesn't
 * match the upi:// scheme, so a plain "name@payflow" QR still works.
 */
function parseUpiPayload(text) {
  if (!text) return null

  if (text.startsWith('upi://')) {
    try {
      const query = text.split('?')[1] || ''
      const params = new URLSearchParams(query)
      const vpa = params.get('pa')
      if (!vpa) return null
      return {
        vpa,
        payeeName: params.get('pn') ? decodeURIComponent(params.get('pn').replace(/\+/g, ' ')) : null,
        amount: params.get('am'),
        note: params.get('tn') ? decodeURIComponent(params.get('tn').replace(/\+/g, ' ')) : null,
      }
    } catch {
      return null
    }
  }

  // Bare VPA fallback, e.g. "rahul@payflow"
  if (/^[\w.+-]+@[\w.-]+$/.test(text.trim())) {
    return { vpa: text.trim(), payeeName: null, amount: null, note: null }
  }

  return null
}

export default function ScanQrPay() {
  const navigate = useNavigate()
  const videoRef = useRef(null)
  const canvasRef = useRef(null)
  const streamRef = useRef(null)
  const rafRef = useRef(null)
  const fileInputRef = useRef(null)

  const [status, setStatus] = useState('starting') // starting | scanning | denied | unsupported | found
  const [scanned, setScanned] = useState(null)

  const stopCamera = useCallback(() => {
    if (rafRef.current) cancelAnimationFrame(rafRef.current)
    streamRef.current?.getTracks().forEach((t) => t.stop())
    streamRef.current = null
  }, [])

  const tick = useCallback(() => {
    const video = videoRef.current
    const canvas = canvasRef.current
    if (!video || !canvas || video.readyState !== video.HAVE_ENOUGH_DATA) {
      rafRef.current = requestAnimationFrame(tick)
      return
    }

    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d', { willReadFrequently: true })
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)

    const code = jsQR(imageData.data, imageData.width, imageData.height, { inversionAttempts: 'dontInvert' })
    if (code?.data) {
      const parsed = parseUpiPayload(code.data)
      if (parsed) {
        setScanned(parsed)
        setStatus('found')
        stopCamera()
        return
      }
    }
    rafRef.current = requestAnimationFrame(tick)
  }, [stopCamera])

  useEffect(() => {
    if (!navigator.mediaDevices?.getUserMedia) {
      setStatus('unsupported')
      return
    }

    navigator.mediaDevices
      .getUserMedia({ video: { facingMode: 'environment' } })
      .then((stream) => {
        streamRef.current = stream
        if (videoRef.current) {
          videoRef.current.srcObject = stream
          videoRef.current.play()
        }
        setStatus('scanning')
        rafRef.current = requestAnimationFrame(tick)
      })
      .catch(() => setStatus('denied'))

    return () => stopCamera()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleFileUpload = (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      canvas.width = img.width
      canvas.height = img.height
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0)
      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
      const code = jsQR(imageData.data, imageData.width, imageData.height)
      const parsed = code?.data ? parseUpiPayload(code.data) : null
      if (parsed) {
        stopCamera()
        setScanned(parsed)
        setStatus('found')
      } else {
        setStatus('scanning')
        alert('Could not read a valid PayFlow/UPI QR code from that image.')
      }
    }
    img.src = URL.createObjectURL(file)
  }

  const proceedToPay = () => {
    const params = new URLSearchParams({ vpa: scanned.vpa })
    if (scanned.amount) params.set('amount', scanned.amount)
    if (scanned.note) params.set('note', scanned.note)
    navigate(`/send?${params.toString()}`)
  }

  return (
    <div className="max-w-sm mx-auto">
      <button onClick={() => navigate(-1)} className="flex items-center gap-1 text-sm text-[var(--color-muted)] mb-4">
        <ArrowLeft size={16} /> Back
      </button>
      <h1 className="font-display text-xl font-semibold mb-6">Scan & pay</h1>

      {status === 'found' && scanned ? (
        <div className="bg-[var(--color-surface)] border border-[var(--color-line)] rounded-2xl p-6 text-center">
          <Camera size={40} className="mx-auto text-[var(--color-success)] mb-3" />
          <p className="text-sm text-[var(--color-muted)]">QR code recognised</p>
          <p className="font-medium mt-1">{scanned.payeeName || scanned.vpa}</p>
          <p className="text-xs text-[var(--color-muted)]">{scanned.vpa}</p>
          {scanned.amount && (
            <p className="font-mono-amount text-2xl font-semibold mt-3">₹{Number(scanned.amount).toLocaleString('en-IN')}</p>
          )}
          <div className="flex gap-3 mt-6">
            <button
              onClick={() => {
                setScanned(null)
                setStatus('scanning')
                navigator.mediaDevices
                  .getUserMedia({ video: { facingMode: 'environment' } })
                  .then((stream) => {
                    streamRef.current = stream
                    if (videoRef.current) {
                      videoRef.current.srcObject = stream
                      videoRef.current.play()
                    }
                    rafRef.current = requestAnimationFrame(tick)
                  })
              }}
              className="flex-1 rounded-lg border border-[var(--color-line)] py-2.5 text-sm font-medium"
            >
              Scan again
            </button>
            <button
              onClick={proceedToPay}
              className="flex-1 rounded-lg bg-[var(--color-primary)] text-white py-2.5 text-sm font-medium"
            >
              Pay now
            </button>
          </div>
        </div>
      ) : (
        <>
          <div className="relative aspect-square rounded-2xl overflow-hidden bg-black">
            <video ref={videoRef} className="w-full h-full object-cover" muted playsInline />
            <canvas ref={canvasRef} className="hidden" />

            {status === 'scanning' && (
              <div className="absolute inset-8 border-2 border-white/70 rounded-2xl pointer-events-none" />
            )}

            {(status === 'starting') && (
              <div className="absolute inset-0 flex items-center justify-center text-white text-sm bg-black/40">
                Starting camera…
              </div>
            )}

            {status === 'denied' && (
              <div className="absolute inset-0 flex flex-col items-center justify-center text-white text-sm bg-black/70 px-6 text-center gap-2">
                <ZapOff size={28} />
                Camera access denied. You can still upload a QR image below, or enter a UPI ID manually.
              </div>
            )}

            {status === 'unsupported' && (
              <div className="absolute inset-0 flex flex-col items-center justify-center text-white text-sm bg-black/70 px-6 text-center gap-2">
                <AlertCircle size={28} />
                Camera not available in this browser. Upload a QR image instead.
              </div>
            )}
          </div>

          <p className="text-xs text-center text-[var(--color-muted)] mt-3">
            Point your camera at a PayFlow QR code
          </p>

          <button
            onClick={() => fileInputRef.current?.click()}
            className="mt-4 w-full flex items-center justify-center gap-2 rounded-lg border border-[var(--color-line)] py-2.5 text-sm font-medium"
          >
            <ImageUp size={16} /> Upload QR image instead
          </button>
          <input ref={fileInputRef} type="file" accept="image/*" onChange={handleFileUpload} className="hidden" />

          <button
            onClick={() => navigate('/send')}
            className="mt-2 w-full text-center text-sm text-[var(--color-primary)] font-medium py-2"
          >
            Enter UPI ID manually instead
          </button>
        </>
      )}
    </div>
  )
}
