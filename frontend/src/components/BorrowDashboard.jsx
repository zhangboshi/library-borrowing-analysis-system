import React from 'react'
import StatsCard from './StatsCard.jsx'

function BorrowDashboard ({ summary, error }) {
  if (error) {
    return <div role="alert">{error}</div>
  }

  if (!summary) {
    return <p>加载中...</p>
  }

  return (
    <div className="grid">
      <StatsCard label="进行中的借阅" value={summary.activeBorrowings} />
      <StatsCard label="已完成的借阅" value={summary.completedBorrowings} />
      <StatsCard label="活跃借阅人" value={summary.activeBorrowers} />
    </div>
  )
}

export default BorrowDashboard
