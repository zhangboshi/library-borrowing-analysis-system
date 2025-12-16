import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import BorrowDashboard from './BorrowDashboard.jsx'

describe('BorrowDashboard', () => {
  it('renders loading state', () => {
    render(<BorrowDashboard summary={null} />)
    expect(screen.getByText('加载中...')).toBeInTheDocument()
  })

  it('renders error state', () => {
    render(<BorrowDashboard summary={null} error="failed" />)
    expect(screen.getByRole('alert')).toHaveTextContent('failed')
  })

  it('renders stats cards', () => {
    const summary = { activeBorrowings: 2, completedBorrowings: 3, activeBorrowers: 1 }
    render(<BorrowDashboard summary={summary} />)
    expect(screen.getByText('进行中的借阅')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
    expect(screen.getByText('3')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()
  })
})
