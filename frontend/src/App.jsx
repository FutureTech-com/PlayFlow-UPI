import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'

import AppLayout from './layouts/AppLayout'
import ProtectedRoute from './components/ProtectedRoute'

// Auth
import Splash from './pages/Splash'
import Login from './pages/Login'
import Register from './pages/Register'

// Main Pages
import Home from './pages/Home'
import More from './pages/More'
import Profile from './pages/Profile'
import Settings from './pages/Settings'
import Notifications from './pages/Notifications'

// Payments
import SendMoney from './pages/SendMoney'
import ReceiveMoney from './pages/ReceiveMoney'
import ScanQrPay from './pages/ScanQrPay'
import RequestMoney from './pages/RequestMoney'
import SelfTransfer from './pages/SelfTransfer'
import MerchantQr from './pages/MerchantQr'

// Banking
import BankAccounts from './pages/BankAccounts'
import WalletPage from './pages/WalletPage'

// Transactions
import TransactionHistory from './pages/TransactionHistory'
import TransactionDetail from './pages/TransactionDetail'

// Features
import Favourites from './pages/Favourites'
import SplitBills from './pages/SplitBills'
import ScheduledPayments from './pages/ScheduledPayments'
import Bills from './pages/Bills'
import Rewards from './pages/Rewards'
import Analytics from './pages/Analytics'
import Security from './pages/Security'
import Complaints from './pages/Complaints'

// Admin
import AdminDashboard from './pages/admin/AdminDashboard'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Splash />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            {/* Home */}
            <Route path="/home" element={<Home />} />
            <Route path="/more" element={<More />} />

            {/* Payments */}
            <Route path="/send" element={<SendMoney />} />
            <Route path="/receive" element={<ReceiveMoney />} />
            <Route path="/scan" element={<ScanQrPay />} />
            <Route path="/request" element={<RequestMoney />} />
            <Route path="/self-transfer" element={<SelfTransfer />} />
            <Route path="/merchant-qr" element={<MerchantQr />} />

            {/* Banking */}
            <Route path="/bank-accounts" element={<BankAccounts />} />
            <Route path="/wallet" element={<WalletPage />} />

            {/* Transactions */}
            <Route path="/transactions" element={<TransactionHistory />} />
            <Route path="/transactions/:id" element={<TransactionDetail />} />

            {/* User */}
            <Route path="/profile" element={<Profile />} />
            <Route path="/notifications" element={<Notifications />} />
            <Route path="/settings" element={<Settings />} />
            <Route path="/security" element={<Security />} />

            {/* Utilities */}
            <Route path="/favourites" element={<Favourites />} />
            <Route path="/split-bills" element={<SplitBills />} />
            <Route path="/scheduled-payments" element={<ScheduledPayments />} />
            <Route path="/bills" element={<Bills />} />
            <Route path="/rewards" element={<Rewards />} />
            <Route path="/analytics" element={<Analytics />} />
            <Route path="/complaints" element={<Complaints />} />

            {/* Admin */}
            <Route path="/admin" element={<AdminDashboard />} />
          </Route>
        </Route>

        {/* Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}