import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { authApi } from '../api/auth'

const storedUser = (() => {
  try {
    const raw = localStorage.getItem('payflow_user')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
})()

const persistSession = (auth) => {
  localStorage.setItem('payflow_access_token', auth.accessToken)
  localStorage.setItem('payflow_refresh_token', auth.refreshToken)
  localStorage.setItem('payflow_user', JSON.stringify(auth.user))
}

export const registerUser = createAsyncThunk('auth/register', async (payload, { rejectWithValue }) => {
  try {
    const data = await authApi.register(payload)
    persistSession(data)
    return data
  } catch (err) {
    return rejectWithValue(err?.response?.data?.message || 'Registration failed')
  }
})

export const loginUser = createAsyncThunk('auth/login', async (payload, { rejectWithValue }) => {
  try {
    const data = await authApi.login(payload)
    persistSession(data)
    return data
  } catch (err) {
    return rejectWithValue(err?.response?.data?.message || 'Invalid email or password')
  }
})

export const loginWithMobileOtp = createAsyncThunk(
  'auth/loginWithMobileOtp',
  async ({ phone, otp }, { rejectWithValue }) => {
    try {
      const data = await authApi.loginWithMobileOtp(phone, otp)
      persistSession(data)
      return data
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message || 'Invalid or expired OTP')
    }
  }
)

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: storedUser,
    isAuthenticated: !!localStorage.getItem('payflow_access_token'),
    status: 'idle',
    error: null,
  },
  reducers: {
    logout(state) {
      localStorage.removeItem('payflow_access_token')
      localStorage.removeItem('payflow_refresh_token')
      localStorage.removeItem('payflow_user')
      state.user = null
      state.isAuthenticated = false
    },
    clearAuthError(state) {
      state.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addMatcher(
        (action) => [registerUser.pending.type, loginUser.pending.type, loginWithMobileOtp.pending.type].includes(action.type),
        (state) => {
          state.status = 'loading'
          state.error = null
        }
      )
      .addMatcher(
        (action) =>
          [registerUser.fulfilled.type, loginUser.fulfilled.type, loginWithMobileOtp.fulfilled.type].includes(action.type),
        (state, action) => {
          state.status = 'succeeded'
          state.user = action.payload.user
          state.isAuthenticated = true
        }
      )
      .addMatcher(
        (action) =>
          [registerUser.rejected.type, loginUser.rejected.type, loginWithMobileOtp.rejected.type].includes(action.type),
        (state, action) => {
          state.status = 'failed'
          state.error = action.payload
        }
      )
  },
})

export const { logout, clearAuthError } = authSlice.actions
export default authSlice.reducer
