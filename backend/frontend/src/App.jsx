import { useMemo, useState } from 'react'

const roleOptions = [
  { label: 'Super Admin', value: 1 },
  { label: 'Manager', value: 2 },
  { label: 'User', value: 3 }
]

const formatReply = (reply) => {
  const date = Number(reply.date || 0)
  const dateLabel = date ? new Date(date).toLocaleString() : 'Unknown time'
  return `[${dateLabel}] ${reply.from}: ${reply.message}`
}

export default function App() {
  const [phone, setPhone] = useState('')
  const [message, setMessage] = useState('')
  const [lastSentPhone, setLastSentPhone] = useState('')
  const [lastSeenTimestamp, setLastSeenTimestamp] = useState(0)
  const [status, setStatus] = useState('Ready.')
  const [gatewayBaseUrl, setGatewayBaseUrl] = useState('')
  const [gatewayToken, setGatewayToken] = useState('')
  const [replies, setReplies] = useState([])
  const [loading, setLoading] = useState(false)

  const [registerForm, setRegisterForm] = useState({
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    contactNumber: '',
    address: '',
    userRole: 3,
    locationId: '',
    managerId: ''
  })
  const [loginForm, setLoginForm] = useState({ email: '', password: '' })
  const [authStatus, setAuthStatus] = useState('Not logged in.')
  const [session, setSession] = useState(null)

  const formattedReplies = useMemo(() => {
    if (!replies.length) {
      return 'No replies loaded yet.'
    }

    return replies.map(formatReply).join('\n')
  }, [replies])

  const register = async () => {
    try {
      const payload = {
        ...registerForm,
        userRole: Number(registerForm.userRole),
        locationId: registerForm.locationId ? Number(registerForm.locationId) : null,
        managerId: registerForm.managerId ? Number(registerForm.managerId) : null
      }
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
      const data = await response.json()
      if (!response.ok) {
        throw new Error(data.error || 'Registration failed')
      }
      setAuthStatus(`Registered ${data.email} successfully.`)
    } catch (error) {
      setAuthStatus(`Register failed: ${error.message}`)
    }
  }

  const login = async () => {
    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginForm)
      })
      const data = await response.json()
      if (!response.ok) {
        throw new Error(data.error || 'Login failed')
      }
      setSession(data)
      setAuthStatus(`Logged in as ${data.user.firstName} ${data.user.lastName} (role ${data.user.userRole}).`)
    } catch (error) {
      setAuthStatus(`Login failed: ${error.message}`)
    }
  }

  const sendMessage = async () => {
    const to = phone.trim()
    const body = message.trim()

    if (!to || !body) {
      setStatus('Phone and message are required.')
      return
    }

    setLoading(true)
    try {
      const response = await fetch('/api/messages/send', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(gatewayBaseUrl.trim() ? { 'X-Gateway-Base-Url': gatewayBaseUrl.trim() } : {}),
          ...(gatewayToken.trim() ? { Authorization: gatewayToken.trim() } : {})
        },
        body: JSON.stringify({ to, message: body })
      })

      const payload = await response.json().catch(() => ({}))
      if (!response.ok) {
        setStatus(`Send failed: ${payload.error || response.statusText}`)
        return
      }

      setLastSentPhone(to)
      setLastSeenTimestamp((current) => current || Date.now())
      setStatus(`Message sent to ${to}. You can now fetch replies.`)
    } catch (error) {
      setStatus(`Send failed: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  const fetchReplies = async () => {
    const targetPhone = lastSentPhone.trim()
    if (!targetPhone) {
      setStatus('Send a message first so the app knows what phone number to check.')
      return
    }

    const params = new URLSearchParams({
      phone: targetPhone,
      limit: '100'
    })

    if (lastSeenTimestamp > 0) {
      params.set('since', String(lastSeenTimestamp))
    }

    setLoading(true)
    try {
      const response = await fetch(`/api/messages/replies?${params.toString()}`, {
        headers: {
          ...(gatewayBaseUrl.trim() ? { 'X-Gateway-Base-Url': gatewayBaseUrl.trim() } : {}),
          ...(gatewayToken.trim() ? { Authorization: gatewayToken.trim() } : {})
        }
      })
      const payload = await response.json().catch(() => [])

      if (!response.ok) {
        setStatus(`Fetch failed: ${payload.error || response.statusText}`)
        return
      }

      if (!Array.isArray(payload) || payload.length === 0) {
        setStatus(`No new replies for ${targetPhone}.`)
        return
      }

      const maxDate = Math.max(...payload.map((item) => Number(item.date) || 0))
      if (maxDate > 0) {
        setLastSeenTimestamp(maxDate + 1)
      }

      setReplies(payload)
      setStatus(`Fetched ${payload.length} reply/replies for ${targetPhone}.`)
    } catch (error) {
      setStatus(`Fetch failed: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="container">
      <h1>SMS Gateway Portal</h1>

      <section className="card">
        <h2>Authentication</h2>
        <p className="status">{authStatus}</p>

        <div className="grid-two">
          <div>
            <h3>Register</h3>
            <input placeholder="Email" value={registerForm.email} onChange={(e) => setRegisterForm((p) => ({ ...p, email: e.target.value }))} />
            <input placeholder="Password" type="password" value={registerForm.password} onChange={(e) => setRegisterForm((p) => ({ ...p, password: e.target.value }))} />
            <input placeholder="First name" value={registerForm.firstName} onChange={(e) => setRegisterForm((p) => ({ ...p, firstName: e.target.value }))} />
            <input placeholder="Last name" value={registerForm.lastName} onChange={(e) => setRegisterForm((p) => ({ ...p, lastName: e.target.value }))} />
            <input placeholder="Contact number" value={registerForm.contactNumber} onChange={(e) => setRegisterForm((p) => ({ ...p, contactNumber: e.target.value }))} />
            <input placeholder="Address" value={registerForm.address} onChange={(e) => setRegisterForm((p) => ({ ...p, address: e.target.value }))} />
            <label>User role</label>
            <select value={registerForm.userRole} onChange={(e) => setRegisterForm((p) => ({ ...p, userRole: Number(e.target.value) }))}>
              {roleOptions.map((opt) => (
                <option key={opt.value} value={opt.value}>{opt.label}</option>
              ))}
            </select>
            <input placeholder="Location ID (optional)" value={registerForm.locationId} onChange={(e) => setRegisterForm((p) => ({ ...p, locationId: e.target.value }))} />
            <input placeholder="Manager ID (required for role 3)" value={registerForm.managerId} onChange={(e) => setRegisterForm((p) => ({ ...p, managerId: e.target.value }))} />
            <button onClick={register}>Register</button>
          </div>

          <div>
            <h3>Login</h3>
            <input placeholder="Email" value={loginForm.email} onChange={(e) => setLoginForm((p) => ({ ...p, email: e.target.value }))} />
            <input placeholder="Password" type="password" value={loginForm.password} onChange={(e) => setLoginForm((p) => ({ ...p, password: e.target.value }))} />
            <button onClick={login}>Login</button>
            {session ? <pre className="replies">{JSON.stringify(session, null, 2)}</pre> : null}
          </div>
        </div>
      </section>

      <section className="card">
        <h2>Send Message</h2>
        <label htmlFor="gatewayBaseUrl">Gateway Base URL (optional override)</label>
        <input id="gatewayBaseUrl" placeholder="http://192.168.1.38:8082" value={gatewayBaseUrl} onChange={(event) => setGatewayBaseUrl(event.target.value)} />

        <label htmlFor="gatewayToken">Gateway Token (optional override)</label>
        <input id="gatewayToken" placeholder="token" value={gatewayToken} onChange={(event) => setGatewayToken(event.target.value)} />

        <label htmlFor="phone">Phone Number</label>
        <input id="phone" placeholder="+639xxxxxxxxx" value={phone} onChange={(event) => setPhone(event.target.value)} />

        <label htmlFor="message">Message</label>
        <textarea id="message" rows={4} placeholder="Type your message" value={message} onChange={(event) => setMessage(event.target.value)} />

        <button disabled={loading} onClick={sendMessage}>Send</button>
      </section>

      <section className="card">
        <h2>Fetch Replies</h2>
        <button disabled={loading} onClick={fetchReplies}>Manually Fetch Replies</button>
        <div className="status">{status}</div>
        <pre className="replies">{formattedReplies}</pre>
      </section>
    </main>
  )
}
