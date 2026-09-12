import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const navItems = [
  { to: '/', label: 'Dashboard', end: true },
  { to: '/transactions', label: 'Transactions' },
  { to: '/categories', label: 'Categories' },
  { to: '/reports', label: 'Reports' }
]

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="layout">
      <header className="layout-header">
        <div className="brand">
          <span className="brand-mark">₹</span>
          <span className="brand-name">ExpenseTracker</span>
        </div>
        <nav className="layout-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="layout-user">
          <div className="user-avatar">{user?.name.charAt(0).toUpperCase()}</div>
          <span className="user-name">{user?.name}</span>
          <button className="btn btn-ghost btn-small" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>
      <main className="layout-content">
        <Outlet />
      </main>
    </div>
  )
}