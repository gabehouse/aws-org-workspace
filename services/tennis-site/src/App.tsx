import { useEffect, useState, useCallback, useMemo, Fragment } from "react";
import type { Schema } from "../amplify/data/resource";
import { useAuthenticator, Authenticator } from '@aws-amplify/ui-react';
import '@aws-amplify/ui-react/styles.css';
import { generateClient } from "aws-amplify/data";
import { fetchAuthSession, fetchUserAttributes } from 'aws-amplify/auth';
import { TIME_SLOTS as timeSlots, getBookableDates, isBookable, slotKey } from '../amplify/shared/schedule';

type Booking = Schema["Booking"]["type"];
type WaitlistEntry = Schema["WaitlistEntry"]["type"];

const SCHEDULE_REFRESH_MS = 30_000;

/** Formats a YYYY-MM-DD string as e.g. "Mon, Jul 15" without shifting it across time zones. */
const formatDisplayDate = (dateSlot: string): string =>
  new Date(`${dateSlot}T12:00:00`).toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });

const formatDisplayName = (
  firstName: string | null | undefined,
  lastName: string | null | undefined,
  email: string | null | undefined,
): string => {
  if (firstName && lastName) {
    return `${firstName} ${lastName.charAt(0)}.`;
  }
  return email || 'Unknown';
};

/** Amplify returns GraphQL errors (including messages thrown by the booking Lambda) instead of throwing. */
async function runMutation(request: Promise<{ errors?: { message: string }[] }>): Promise<void> {
  const { errors } = await request;
  if (errors?.length) {
    throw new Error(errors.map((e) => e.message).join(', '));
  }
}

const errorMessage = (error: unknown, fallback: string): string =>
  error instanceof Error && error.message ? error.message : fallback;

interface BookerDetails {
  dateSlot: string;
  timeSlot: string;
  firstName: string;
  lastName: string;
  email: string;
}

function App() {
  const client = useMemo(() => generateClient<Schema>(), []);

  const { user, signOut, authStatus } = useAuthenticator((context) => [
    context.user,
    context.authStatus
  ]);
  const isSignedIn = authStatus === 'authenticated' && !!user;
  const authMode = isSignedIn ? 'userPool' : 'apiKey';

  const [bookedSlots, setBookedSlots] = useState<Set<string>>(new Set());
  const [bookings, setBookings] = useState<Map<string, Booking>>(new Map());
  const [isLoading, setIsLoading] = useState(true);
  const [modalContent, setModalContent] = useState<string | null>(null);
  const [showAuth, setShowAuth] = useState(false);

  const [waitlistEntries, setWaitlistEntries] = useState<WaitlistEntry[]>([]);
  const [showWaitlistModal, setShowWaitlistModal] = useState(false);
  const [currentUserEmail, setCurrentUserEmail] = useState<string | null>(null);

  const [showBookerDetailsModal, setShowBookerDetailsModal] = useState(false);
  const [bookerDetails, setBookerDetails] = useState<BookerDetails | null>(null);

  const [isAdmin, setIsAdmin] = useState(false);

  const sevenDates = useMemo(
    () => getBookableDates().map((storage) => ({ storage, display: formatDisplayDate(storage) })),
    [],
  );

  const hideModal = useCallback(() => {
    const timer = setTimeout(() => {
      setModalContent(null);
    }, 3000);
    return () => clearTimeout(timer);
  }, []);

  // Slots (public) say which times are taken; Bookings (private) are only returned for the
  // signed-in user's own slots, or for every slot if they are an admin.
  const loadSchedule = useCallback(async () => {
    try {
      const dates = sevenDates.map((d) => d.storage);
      const slotPages = await Promise.all(dates.map((dateSlot) => client.models.Slot.list({ dateSlot, authMode })));
      setBookedSlots(new Set(slotPages.flatMap((page) => page.data).map((s) => slotKey(s.dateSlot, s.timeSlot))));

      if (isSignedIn) {
        const bookingPages = await Promise.all(dates.map((dateSlot) => client.models.Booking.list({ dateSlot, authMode })));
        setBookings(new Map(bookingPages.flatMap((page) => page.data).map((b) => [slotKey(b.dateSlot, b.timeSlot), b])));
      } else {
        setBookings(new Map());
      }
    } catch (error) {
      console.error("Error loading schedule:", error);
      setModalContent("Failed to load the schedule. Please refresh the page.");
      hideModal();
    } finally {
      setIsLoading(false);
    }
  }, [client, authMode, isSignedIn, sevenDates, hideModal]);

  useEffect(() => {
    if (authStatus === 'configuring') {
      return;
    }
    loadSchedule();
    const interval = setInterval(loadSchedule, SCHEDULE_REFRESH_MS);
    return () => clearInterval(interval);
  }, [authStatus, loadSchedule]);

  // The UI only shows admin controls; the backend enforces the Admins group on every request.
  useEffect(() => {
    if (!isSignedIn) {
      setIsAdmin(false);
      setCurrentUserEmail(null);
      return;
    }
    fetchAuthSession()
      .then((session) => {
        const groups = session.tokens?.accessToken.payload['cognito:groups'];
        setIsAdmin(Array.isArray(groups) && groups.includes('Admins'));
      })
      .catch(() => setIsAdmin(false));
    fetchUserAttributes()
      .then((attrs) => setCurrentUserEmail(attrs.email || user.username))
      .catch(() => setCurrentUserEmail(user.username));
  }, [isSignedIn, user]);

  // Waitlist entries are owner-scoped: users see their own entry, admins see all of them.
  useEffect(() => {
    if (!isSignedIn) {
      setWaitlistEntries([]);
      return;
    }
    const sub = client.models.WaitlistEntry.observeQuery({ authMode: 'userPool' }).subscribe({
      next: ({ items }) => setWaitlistEntries(items),
      error: (error) => console.error("Error observing waitlist entries:", error),
    });
    return () => sub.unsubscribe();
  }, [client, isSignedIn]);

  const isUserInWaitlist = !!currentUserEmail && waitlistEntries.some((entry) => entry.email === currentUserEmail);

  useEffect(() => {
    if (authStatus === 'authenticated' && showAuth) {
      setShowAuth(false);
      setModalContent("Successfully signed in!");
      hideModal();
    }
  }, [authStatus, showAuth, hideModal]);

  const handleRemoveBookingAsAdmin = async (dateSlot: string, timeSlot: string) => {
    try {
      await runMutation(client.mutations.cancelBooking({ dateSlot, timeSlot }));
      setModalContent("Booking removed.");
    } catch (error) {
      console.error("Error removing booking as admin:", error);
      setModalContent(errorMessage(error, "Failed to remove booking."));
    }
    setShowBookerDetailsModal(false);
    hideModal();
    await loadSchedule();
  };

  const handleSlotClick = async (dateSlot: string, timeSlot: string) => {
    if (!isSignedIn) {
      setModalContent("Please sign in to book or unbook a slot.");
      setShowAuth(true);
      return;
    }

    const key = slotKey(dateSlot, timeSlot);
    const booking = bookings.get(key);
    const label = `${formatDisplayDate(dateSlot)} ${timeSlot}`;

    if (booking && booking.owner !== user.userId) {
      if (isAdmin) {
        setBookerDetails({
          dateSlot,
          timeSlot,
          firstName: booking.firstName || 'N/A',
          lastName: booking.lastName || 'N/A',
          email: booking.email || 'N/A',
        });
        setShowBookerDetailsModal(true);
      }
      return;
    }

    try {
      if (booking) {
        await runMutation(client.mutations.cancelBooking({ dateSlot, timeSlot }));
        setModalContent(`Slot ${label} unbooked.`);
      } else if (bookedSlots.has(key)) {
        setModalContent(`Slot ${label} is already booked.`);
      } else {
        await runMutation(client.mutations.bookSlot({ dateSlot, timeSlot }));
        setModalContent(`Slot ${label} booked. See you on the court!`);
      }
    } catch (error) {
      console.error("Error booking/unbooking slot:", error);
      setModalContent(errorMessage(error, "Failed to update slot. Please try again."));
    }
    hideModal();
    await loadSchedule();
  };

  const handleWaitlistToggle = async () => {
    if (!isSignedIn) {
      setModalContent("Please sign in to manage your waitlist status.");
      setShowAuth(true);
      hideModal();
      return;
    }

    try {
      if (isUserInWaitlist) {
        const entryToRemove = waitlistEntries.find((entry) => entry.email === currentUserEmail);
        if (entryToRemove) {
          await runMutation(client.models.WaitlistEntry.delete({ id: entryToRemove.id }, { authMode: 'userPool' }));
        }
        setModalContent("You have been removed from the group lesson waitlist.");
      } else {
        const attrs = await fetchUserAttributes();
        await runMutation(client.models.WaitlistEntry.create({
          email: attrs.email || user.username,
          firstName: attrs.given_name || null,
          lastName: attrs.family_name || null,
          createdAt: new Date().toISOString(),
        }, { authMode: 'userPool' }));
        setModalContent("You have been added to the group lesson waitlist! We'll notify you when spots become available.");
      }
    } catch (error) {
      console.error("Error managing waitlist:", error);
      setModalContent(errorMessage(error, "Failed to update waitlist status. Please try again."));
    }
    hideModal();
  };

  const handleViewWaitlist = () => {
    setShowWaitlistModal(true);
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', backgroundColor: '#f3f4f6' }}>
        <p style={{ fontSize: '1.125rem', color: '#4b5563' }}>Loading schedule...</p>
      </div>
    );
  }

  return (
    <main style={{
      minHeight: '100vh',
      // Removed backgroundColor to allow index.css gradient to show
      fontFamily: 'sans-serif',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      width: '100vw', // Ensure main takes full viewport width
      overflowX: 'hidden', // Prevent main from causing horizontal scroll
      boxSizing: 'border-box', // Include padding in width calculation for main
    }}>
      <div style={{
        width: '100%', // Take full width of its parent (main, which is 100vw)
        maxWidth: '64rem',
        backgroundColor: '#ffffff',
        borderRadius: '0.5rem',
        boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
        padding: 'clamp(1rem, 5vw, 1.5rem)', // Responsive padding for content
        flexShrink: 0,
        maxHeight: 'calc(100vh - 2rem)',
        overflowY: 'auto', // Allow vertical scrolling for the content box
        position: 'relative',
        boxSizing: 'border-box', // Include padding in width calculation for this div
      }}>

        {/* Header section with text title and sign-in/out button */}
        <div style={{
          marginBottom: '1.5rem',
          display: 'flex',
          flexDirection: 'column', // Stack vertically by default
          alignItems: 'center', // Center items horizontally
          justifyContent: 'center',
          paddingBottom: '0.5rem',
          borderBottom: '1px solid #e5e7eb',
          gap: '1rem', // Space between title and button
        }}>
          {/* Sign In / Sign Out Button - Positioned to the top right */}
          <div style={{
            width: '100%', // Take full width to allow text-align
            textAlign: 'right', // Align button to the right
            paddingRight: '0.5rem', // Small padding from the right edge
            boxSizing: 'border-box',
          }}>
            {user ? (
              <button
                onClick={signOut}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: '#f0f0f0', // Light gray
                  color: '#4a4a4a', // Darker gray text
                  fontWeight: '600',
                  borderRadius: '0.375rem',
                  boxShadow: '0 1px 2px 0 rgba(0, 0, 0, 0.05)', // Softer shadow
                  transition: 'background-color 0.2s ease-in-out, box-shadow 0.2s ease-in-out',
                  border: '1px solid #d0d0d0', // Subtle border
                  cursor: 'pointer',
                  fontSize: '0.875rem', // Smaller font size for mobile
                }}
                onMouseOver={(e) => {
                  e.currentTarget.style.backgroundColor = '#e0e0e0'; // Slightly darker gray on hover
                  e.currentTarget.style.boxShadow = '0 2px 4px 0 rgba(0, 0, 0, 0.1)'; // Slightly more pronounced shadow on hover
                }}
                onMouseOut={(e) => {
                  e.currentTarget.style.backgroundColor = '#f0f0f0';
                  e.currentTarget.style.boxShadow = '0 1px 2px 0 rgba(0, 0, 0, 0.05)';
                }}
              >
                Sign out
              </button>
            ) : (
              <button
                onClick={() => setShowAuth(true)}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: '#2563eb',
                  color: '#ffffff',
                  fontWeight: '600',
                  borderRadius: '0.375rem',
                  boxShadow: '0 2px 4px -1px rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.06)',
                  transition: 'background-color 0.2s ease-in-out',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '0.875rem', // Smaller font size for mobile
                }}
                onMouseOver={(e) => (e.currentTarget.style.backgroundColor = '#1d4ed8')}
                onMouseOut={(e) => (e.currentTarget.style.backgroundColor = '#2563eb')}
              >
                Sign In / Sign Up
              </button>
            )}
          </div>

          {/* Text Title */}
          <h1 style={{
            fontSize: 'clamp(1.5rem, 5vw, 2.5rem)', // Responsive font size
            fontWeight: 'bold',
            color: '#1f2937',
            textAlign: 'center',
            margin: '0', // Remove default margins
            marginTop: '-1rem', // Adjust as needed to reduce gap with button
          }}>
            Grand River Tennis
          </h1>
        </div>

        <div style={{ marginBottom: '1.5rem', textAlign: 'left', color: '#374151', padding: '0 1rem' }}>
          <p style={{ fontSize: '1.1rem', fontWeight: '600', marginBottom: '0.75rem', color: '#111827' }}>
            Private tennis lessons in Waterloo
          </p>

          <p style={{ fontSize: '1rem', lineHeight: '1.6', marginBottom: '1rem' }}>
            Hi, I’m Gabriel. I coach players of all levels, and I’ve led tennis camps and been a hitting partner for top junior OTA players. Lessons are at the WCI public courts.
          </p>

          <p style={{ fontSize: '1rem', lineHeight: '1.6', marginBottom: '1rem' }}>
            <strong>$30 an hour</strong>, split however you like with friends. Your first lesson is <strong>$10</strong>. Sign in and pick an open time below to book.
          </p>

          <p style={{ fontSize: '0.9rem', color: '#6B7280' }}>
            No cancellation fees, though a few hours’ notice is appreciated. Cash or e-transfer. Questions? Email <a href="mailto:gabriel.jsh@gmail.com" style={{ color: '#2563eb', textDecoration: 'underline' }}>gabriel.jsh@gmail.com</a>.
          </p>
        </div>

        <div style={{ marginBottom: '1rem', textAlign: 'left', color: '#374151', padding: '0 1rem' }}>
          <div style={{ backgroundColor: '#F3F4F6', padding: '1rem', borderRadius: '8px', borderLeft: '4px solid #3B82F6' }}>
            <p style={{ fontSize: '1rem', lineHeight: '1.5', margin: 0 }}>
              <strong>Group lessons:</strong> I’m planning small group clinics. Join the waitlist to hear when one opens.
            </p>
          </div>
        </div>

        <div style={{ textAlign: 'center', marginBottom: '2rem', display: 'flex', justifyContent: 'center', gap: '1rem' }}>
          <button
            onClick={handleWaitlistToggle}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: isUserInWaitlist ? '#047857' : '#10b981',
              color: '#ffffff',
              fontWeight: '600',
              borderRadius: '0.5rem',
              boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
              transition: 'background-color 0.2s ease-in-out',
              border: 'none',
              cursor: 'pointer',
            }}
            onMouseOver={(e) => (e.currentTarget.style.backgroundColor = isUserInWaitlist ? '#065f46' : '#059669')}
            onMouseOut={(e) => (e.currentTarget.style.backgroundColor = isUserInWaitlist ? '#047857' : '#10b981')}
          >
            {isUserInWaitlist ? "In Waitlist for Group Lessons (Click to Remove)" : "Sign up for Group Lesson Waitlist"}
          </button>

          {isAdmin && (
            <button
              onClick={handleViewWaitlist}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#3b82f6',
                color: '#ffffff',
                fontWeight: '600',
                borderRadius: '0.5rem',
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
                transition: 'background-color 0.2s ease-in-out',
                border: 'none',
                cursor: 'pointer',
              }}
              onMouseOver={(e) => (e.currentTarget.style.backgroundColor = '#2563eb')}
              onMouseOut={(e) => (e.currentTarget.style.backgroundColor = '#3b82f6')}
            >
              View Waitlist
            </button>
          )}
        </div>

        <div style={{ overflowX: 'auto', width: '100%', boxSizing: 'border-box' }}>
          <div style={{
            display: 'grid',
            gridTemplateColumns: `80px repeat(7, minmax(80px, 1fr))`, // Fixed 80px for the time column
            gap: '0.25rem',
            fontSize: '0.875rem',
            minWidth: '750px', // Increased minWidth to ensure horizontal scroll
          }}>
            <div style={{ padding: '0.5rem', borderBottom: '1px solid #d1d5db', borderRight: '1px solid #d1d5db', backgroundColor: '#f9fafb', fontWeight: '600', color: '#374151', borderRadius: '0.5rem 0 0 0', height: '60px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}></div>
            {sevenDates.map((dateObj) => (
              <div
                key={dateObj.storage}
                style={{ padding: '0.5rem', borderBottom: '1px solid #d1d5db', backgroundColor: '#f9fafb', fontWeight: '600', textAlign: 'center', color: '#374151', height: '60px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
              >
                {dateObj.display}
              </div>
            ))}

            {timeSlots.map((time) => (
              <Fragment key={`row-${time}`}>
                <div
                  key={`time-${time}`}
                  style={{ padding: '0.5rem', borderRight: '1px solid #d1d5db', backgroundColor: '#f9fafb', fontWeight: '600', color: '#374151', textAlign: 'right', paddingRight: '1rem', height: '60px', display: 'flex', alignItems: 'center', justifyContent: 'flex-end' }}
                >
                  {time}
                </div>
                {sevenDates.map((dateObj) => {
                  const key = slotKey(dateObj.storage, time);
                  const booking = bookings.get(key);
                  const isBooked = bookedSlots.has(key);
                  const isBookedByCurrentUser = !!booking && booking.owner === user?.userId;
                  const isPast = !isBooked && !isBookable(dateObj.storage, time);

                  let slotCursor = 'pointer';
                  let slotClickHandler: (() => void) | undefined = () => handleSlotClick(dateObj.storage, time);

                  let slotBackgroundColor: string;
                  let slotBorderColor: string;
                  let slotTextColor: string;


                  if (isBookedByCurrentUser) {
                    slotBackgroundColor = '#dcfce7';
                    slotBorderColor = '#86efad';
                    slotTextColor = '#166534';
                  } else if (isBooked) {
                    if (isAdmin) {
                      slotBackgroundColor = '#f5f5dc';
                      slotBorderColor = '#d4c0a1';
                      slotTextColor = '#5c4033';
                      slotCursor = 'pointer';
                    } else {
                      slotBackgroundColor = '#e0e0e0';
                      slotBorderColor = '#c0c0c0';
                      slotTextColor = '#606060';
                      slotCursor = 'default';
                      slotClickHandler = undefined;
                    }
                  } else if (isPast) {
                    slotBackgroundColor = '#f3f4f6';
                    slotBorderColor = '#e5e7eb';
                    slotTextColor = '#9ca3af';
                    slotCursor = 'default';
                    slotClickHandler = undefined;
                  } else {
                    slotBackgroundColor = '#dbeafe';
                    slotBorderColor = '#93c5fd';
                    slotTextColor = '#1e40af';
                  }


                  return (
                    <div
                      key={`${dateObj.storage}-${time}`}
                      style={{
                        padding: '0.5rem',
                        border: `1px solid ${slotBorderColor}`,
                        cursor: slotCursor,
                        borderRadius: '0.375rem',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        textAlign: 'center',
                        transition: 'background-color 0.2s ease-in-out',
                        height: '60px',
                        backgroundColor: slotBackgroundColor,
                        color: slotTextColor,
                        fontWeight: isBooked ? '500' : 'normal',
                      }}
                      onClick={slotClickHandler}
                      aria-label={isBooked && !isBookedByCurrentUser && !isAdmin ? "This slot is booked and unavailable" : undefined}
                    >
                      {isBookedByCurrentUser ? (
                        <span style={{ fontSize: '0.75rem', overflow: 'hidden', whiteSpace: 'nowrap', textOverflow: 'ellipsis' }}>
                          You
                        </span>
                      ) : isBooked ? (
                        <span style={{ fontSize: '0.75rem', overflow: 'hidden', whiteSpace: 'nowrap', textOverflow: 'ellipsis' }}>
                          {isAdmin && booking ? (
                            formatDisplayName(booking.firstName, booking.lastName, booking.email)
                          ) : (
                            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor" style={{ width: '1em', height: '1em', verticalAlign: 'middle', marginRight: '0.25em' }}>
                              <path fillRule="evenodd" d="M12 1.5a5.25 5.25 0 00-5.25 5.25v3a3 3 0 00-3 3v6.75a3 3 0 003 3h10.5a3 3 0 003-3v-6.75a3 3 0 00-3-3v-3c0-2.9-2.35-5.25-5.25-5.25zm3.75 8.25v-3a3.75 3.75 0 10-7.5 0v3h7.5z" clipRule="evenodd" />
                            </svg>
                          )}
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.75rem' }}>&nbsp;</span>
                      )}
                    </div>
                  );
                })}
              </Fragment>
            ))}
          </div>
        </div>

        <p style={{ marginTop: '1.5rem', marginBottom: 0, textAlign: 'center', fontSize: '0.8rem', color: '#9ca3af' }}>
          Built on AWS with AppSync, DynamoDB Streams, Lambda, and SES ·{' '}
          <a
            href="https://github.com/gabehouse/aws-org-workspace/tree/master/services/tennis-site"
            target="_blank"
            rel="noopener noreferrer"
            style={{ color: '#6b7280', textDecoration: 'underline' }}
          >
            How this site works
          </a>
        </p>
      </div>
      {modalContent && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 999,
        }}>
          <div style={{
            backgroundColor: '#ffffff',
            padding: '2rem',
            borderRadius: '0.5rem',
            boxShadow: '0 10px 20px rgba(0, 0, 0, 0.2)',
            maxWidth: '90%',
            maxHeight: '90%',
            overflowY: 'auto',
            position: 'relative',
            textAlign: 'center',
            color: '#1f2937',
            fontSize: '1.125rem',
          }}>
            <p style={{ margin: '0' }}>{modalContent}</p>
          </div>
        </div>
      )}

      {showWaitlistModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1000,
        }}>
          <div style={{
            backgroundColor: '#ffffff',
            padding: '2rem',
            borderRadius: '0.5rem',
            boxShadow: '0 10px 20px rgba(0, 0, 0, 0.2)',
            maxWidth: '90%',
            maxHeight: '90%',
            overflowY: 'auto',
            position: 'relative',
            textAlign: 'center',
            color: '#1f2937',
            fontSize: '1.125rem',
          }}>
            <button
              onClick={() => setShowWaitlistModal(false)}
              style={{
                position: 'absolute',
                top: '0.75rem',
                right: '0.75rem',
                backgroundColor: 'transparent',
                border: 'none',
                fontSize: '1.5rem',
                cursor: 'pointer',
                color: '#6b7280',
                padding: '0.25rem',
                lineHeight: '1',
                borderRadius: '0.25rem',
                transition: 'color 0.2s ease-in-out',
              }}
              onMouseOver={(e) => (e.currentTarget.style.color = '#374151')}
              onMouseOut={(e) => (e.currentTarget.style.color = '#6b7280')}
            >
              &times;
            </button>
            <h2 style={{ marginTop: '0', marginBottom: '1rem', fontSize: '1.5rem', fontWeight: 'bold' }}>Group Lesson Waitlist</h2>
            {waitlistEntries.length > 0 ? (
              <ul style={{ listStyleType: 'none', padding: 0, margin: 0 }}>
                {waitlistEntries.sort((a, b) => new Date(a.createdAt!).getTime() - new Date(b.createdAt!).getTime()).map((entry) => (
                  <li key={entry.id} style={{ padding: '0.5rem 0', borderBottom: '1px solid #eee' }}>
                    <strong>Name:</strong> {entry.firstName === null ? 'null' : entry.firstName || 'N/A'} {entry.lastName === null ? 'null' : entry.lastName || 'N/A'} <br />
                    <strong>Email:</strong> {entry.email} (Signed up: {new Date(entry.createdAt!).toLocaleString()})
                  </li>
                ))}
              </ul>
            ) : (
              <p>No users currently on the waitlist.</p>
            )}
          </div>
        </div>
      )}

      {showAuth && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(0, 0, 0, 0.7)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1001,
        }}>
          <div style={{
            backgroundColor: '#ffffff',
            position: 'relative',
            maxWidth: '90%',
            maxHeight: '90%',
            overflowY: 'auto',
          }}>
            <Authenticator
              initialState={authStatus === 'authenticated' ? 'signIn' : 'signIn'}
              hideSignUp={false}
            >
              {() => (
                <div style={{ textAlign: 'center', padding: '1rem' }}>
                  <p>Authentication flow completed. You can now close this window.</p>
                  <button
                    onClick={() => setShowAuth(false)}
                    style={{
                      padding: '0.5rem 1rem',
                      backgroundColor: '#2563eb',
                      color: '#ffffff',
                      fontWeight: '600',
                      borderRadius: '0.375rem',
                      border: 'none',
                      cursor: 'pointer',
                      marginTop: '1rem',
                    }}
                  >
                    Close
                  </button>
                </div>
              )}
            </Authenticator>
          </div>
          <button
            onClick={() => setShowAuth(false)}
            style={{
              position: 'absolute',
              top: '1rem',
              right: '1rem',
              backgroundColor: 'transparent',
              border: 'none',
              fontSize: '2rem',
              cursor: 'pointer',
              color: '#fff',
            }}
          >
            &times;
          </button>
        </div>
      )}

      {showBookerDetailsModal && bookerDetails && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          width: '100%',
          height: '100%',
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          zIndex: 1002,
        }}>
          <div style={{
            backgroundColor: '#ffffff',
            padding: '2rem',
            borderRadius: '0.5rem',
            boxShadow: '0 10px 20px rgba(0, 0, 0, 0.2)',
            maxWidth: '400px',
            textAlign: 'center',
            color: '#1f2937',
            position: 'relative',
          }}>
            <button
              onClick={() => setShowBookerDetailsModal(false)}
              style={{
                position: 'absolute',
                top: '0.75rem',
                right: '0.75rem',
                backgroundColor: 'transparent',
                border: 'none',
                fontSize: '1.5rem',
                cursor: 'pointer',
                color: '#6b7280',
                padding: '0.25rem',
                lineHeight: '1',
                borderRadius: '0.25rem',
                transition: 'color 0.2s ease-in-out',
              }}
              onMouseOver={(e) => (e.currentTarget.style.color = '#374151')}
              onMouseOut={(e) => (e.currentTarget.style.color = '#6b7280')}
            >
              &times;
            </button>
            <h2 style={{ marginTop: '0', marginBottom: '1rem', fontSize: '1.5rem', fontWeight: 'bold' }}>Booker Details</h2>
            <p style={{ margin: '0.5rem 0' }}><strong>Date:</strong> {formatDisplayDate(bookerDetails.dateSlot)}</p>
            <p style={{ margin: '0.5rem 0' }}><strong>Time:</strong> {bookerDetails.timeSlot}</p>
            <p style={{ margin: '0.5rem 0' }}><strong>First Name:</strong> {bookerDetails.firstName}</p>
            <p style={{ margin: '0.5rem 0' }}><strong>Last Name:</strong> {bookerDetails.lastName}</p>
            <p style={{ margin: '0.5rem 0' }}><strong>Email:</strong> {bookerDetails.email}</p>
            <button
              onClick={() => handleRemoveBookingAsAdmin(bookerDetails.dateSlot, bookerDetails.timeSlot)}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#dc2626',
                color: '#ffffff',
                fontWeight: '600',
                borderRadius: '0.5rem',
                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.06)',
                transition: 'background-color 0.2s ease-in-out',
                border: 'none',
                cursor: 'pointer',
                marginTop: '1.5rem',
              }}
              onMouseOver={(e) => (e.currentTarget.style.backgroundColor = '#b91c1c')}
              onMouseOut={(e) => (e.currentTarget.style.backgroundColor = '#dc2626')}
            >
              Remove Booking
            </button>
          </div>
        </div>
      )}
    </main>
  );
}

export default App;
