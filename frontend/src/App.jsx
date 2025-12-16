import React, { useEffect, useState } from 'react'
import BorrowDashboard from './components/BorrowDashboard.jsx'
import { fetchSummary } from './api/client.js'

function App () {
  const [summary, setSummary] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    fetchSummary()
      .then(setSummary)
      .catch(() => setError('无法获取统计数据'))
  }, [])

  return (
    <div className="page">
      <header>
        <h1>Library Borrowing Analysis</h1>
      </header>
      <main>
        <BorrowDashboard summary={summary} error={error} />
      </main>
    </div>
  )
}

export default App
