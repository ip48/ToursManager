import { useState, useEffect } from 'react'

function App() {
  const [message, setMessage] = useState('')

  useEffect(() => {
    // fetch('/api/hello')
    //   .then(res => res.text())
    //   .then(data => setMessage(data))
    //   .catch(() => setMessage('Hello World!'))
    setMessage('Hello World!')
  }, [])

  return (
    <div style={{ padding: '20px', textAlign: 'center' }}>
      <h1>{message}</h1>
    </div>
  )
}

export default App