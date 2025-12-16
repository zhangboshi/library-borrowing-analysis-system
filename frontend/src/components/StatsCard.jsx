import React from 'react'

function StatsCard ({ label, value }) {
  return (
    <div className="card">
      <p className="label">{label}</p>
      <p className="value">{value}</p>
    </div>
  )
}

export default StatsCard
