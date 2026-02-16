import { useMemo, useState } from 'react'

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

  const formattedReplies = useMemo(() => {
    if (!replies.length) {
      return 'No replies loaded yet.'
    }

    return replies.map(formatReply).join('\n')
  }, [replies])

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
          ...(gatewayToken.trim() ? { 'X-Gateway-Token': gatewayToken.trim() } : {})
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
          ...(gatewayToken.trim() ? { 'X-Gateway-Token': gatewayToken.trim() } : {})
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
      <h1>SMS Gateway UI</h1>

      <section className="card">
        <h2>Send Message</h2>


        <label htmlFor="gatewayBaseUrl">Gateway Base URL (optional override)</label>
        <input
          id="gatewayBaseUrl"
          placeholder="http://192.168.1.38:8082"
          value={gatewayBaseUrl}
          onChange={(event) => setGatewayBaseUrl(event.target.value)}
        />

        <label htmlFor="gatewayToken">Gateway Token (optional override)</label>
        <input
          id="gatewayToken"
          placeholder="acbc45e4-c9c1-469e-b5bc-77290cc5c907"
          value={gatewayToken}
          onChange={(event) => setGatewayToken(event.target.value)}
        />

        <label htmlFor="phone">Phone Number</label>
        <input
          id="phone"
          placeholder="+639xxxxxxxxx"
          value={phone}
          onChange={(event) => setPhone(event.target.value)}
        />

        <label htmlFor="message">Message</label>
        <textarea
          id="message"
          rows={4}
          placeholder="Type your message"
          value={message}
          onChange={(event) => setMessage(event.target.value)}
        />

        <button disabled={loading} onClick={sendMessage}>
          Send
        </button>
      </section>

      <section className="card">
        <h2>Fetch Replies</h2>
        <p>Fetches replies for the latest number you sent to.</p>
        <button disabled={loading} onClick={fetchReplies}>
          Manually Fetch Replies
        </button>

        <div className="status">{status}</div>
        <pre className="replies">{formattedReplies}</pre>
      </section>
    </main>
  )
}
