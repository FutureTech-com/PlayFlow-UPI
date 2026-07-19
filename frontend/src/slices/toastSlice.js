import { createSlice, nanoid } from '@reduxjs/toolkit'

const toastSlice = createSlice({
  name: 'toast',
  initialState: { items: [] },
  reducers: {
    pushToast: {
      reducer(state, action) {
        state.items.push(action.payload)
      },
      prepare(message, variant = 'info') {
        return { payload: { id: nanoid(), message, variant } }
      },
    },
    dismissToast(state, action) {
      state.items = state.items.filter((t) => t.id !== action.payload)
    },
  },
})

export const { pushToast, dismissToast } = toastSlice.actions
export default toastSlice.reducer
