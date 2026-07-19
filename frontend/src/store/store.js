import { configureStore } from '@reduxjs/toolkit'
import authReducer from '../slices/authSlice'
import walletReducer from '../slices/walletSlice'
import toastReducer from '../slices/toastSlice'

export const store = configureStore({
  reducer: {
    auth: authReducer,
    wallet: walletReducer,
    toast: toastReducer,
  },
})
