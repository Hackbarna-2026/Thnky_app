import Button from '../components/Button.jsx'
import ProfileSummary from '../components/profile/ProfileSummary.jsx'

export default function ProfileScreen({ profile, onLogout }) {
  return (
    <>
      <h2 style={{ fontSize: 28, marginBottom: 16 }}>Profile</h2>
      <ProfileSummary profile={profile} />
      <div style={{ marginTop: 24 }}>
        <Button variant="secondary" onClick={onLogout}>
          Log out
        </Button>
      </div>
    </>
  )
}
