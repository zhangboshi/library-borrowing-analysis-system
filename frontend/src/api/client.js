const API_BASE = `${__API_URL__}/api`

export async function fetchSummary () {
  const response = await fetch(`${API_BASE}/stats/summary`, {
    headers: {
      'X-Auth-Token': localStorage.getItem('authToken') || ''
    }
  })

  if (!response.ok) {
    throw new Error('Failed to fetch summary')
  }
  return await response.json()
}
