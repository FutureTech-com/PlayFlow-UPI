import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { bankApi } from '../api/bank'
import { upiApi } from '../api/upi'

export const fetchBankAccounts = createAsyncThunk('wallet/fetchBankAccounts', async () => {
  return bankApi.list()
})

export const linkBankAccount = createAsyncThunk('wallet/linkBankAccount', async (payload, { dispatch, rejectWithValue }) => {
  try {
    const account = await bankApi.link(payload)
    dispatch(fetchBankAccounts())
    return account
  } catch (err) {
    return rejectWithValue(err?.response?.data?.message || 'Could not link account')
  }
})

export const linkBankAccountByMobile = createAsyncThunk(
  'wallet/linkBankAccountByMobile',
  async ({ mobileNumber, accountIndex }, { dispatch, rejectWithValue }) => {
    try {
      const account = await bankApi.linkByMobile(mobileNumber, accountIndex)
      dispatch(fetchBankAccounts())
      return account
    } catch (err) {
      return rejectWithValue(err?.response?.data?.message || 'Could not link account')
    }
  }
)

export const setPrimaryAccount = createAsyncThunk('wallet/setPrimaryAccount', async (id, { dispatch }) => {
  await bankApi.setPrimary(id)
  dispatch(fetchBankAccounts())
})

export const fetchUpiIds = createAsyncThunk('wallet/fetchUpiIds', async () => {
  return upiApi.list()
})

export const createUpiId = createAsyncThunk('wallet/createUpiId', async (payload, { dispatch, rejectWithValue }) => {
  try {
    const upi = await upiApi.create(payload)
    dispatch(fetchUpiIds())
    return upi
  } catch (err) {
    return rejectWithValue(err?.response?.data?.message || 'Could not create UPI ID')
  }
})

const walletSlice = createSlice({
  name: 'wallet',
  initialState: {
    bankAccounts: [],
    upiIds: [],
    status: 'idle',
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchBankAccounts.fulfilled, (state, action) => {
        state.bankAccounts = action.payload
        state.status = 'succeeded'
      })
      .addCase(fetchUpiIds.fulfilled, (state, action) => {
        state.upiIds = action.payload
      })
      .addCase(linkBankAccount.rejected, (state, action) => {
        state.error = action.payload
      })
      .addCase(linkBankAccountByMobile.rejected, (state, action) => {
        state.error = action.payload
      })
      .addCase(createUpiId.rejected, (state, action) => {
        state.error = action.payload
      })
  },
})

export default walletSlice.reducer
