const liveRoutes = {
  collection: "/api/live",

  byId: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}`,

  start: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/start`,

  end: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/end`,

  token: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/token`,

  participants: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/participants`,

  guestRequests: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/guest-requests`,

  acceptGuestRequest: (liveId, requestId) =>
    `/api/live/${encodeURIComponent(liveId)}/guest-requests/${encodeURIComponent(requestId)}/accept`,

  rejectGuestRequest: (liveId, requestId) =>
    `/api/live/${encodeURIComponent(liveId)}/guest-requests/${encodeURIComponent(requestId)}/reject`,

  removeParticipant: (liveId, participantId) =>
    `/api/live/${encodeURIComponent(liveId)}/participants/${encodeURIComponent(participantId)}/remove`,

  muteParticipant: (liveId, participantId) =>
    `/api/live/${encodeURIComponent(liveId)}/participants/${encodeURIComponent(participantId)}/mute`,

  comments: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/comments`,

  comment: (liveId, commentId) =>
    `/api/live/${encodeURIComponent(liveId)}/comments/${encodeURIComponent(commentId)}`,

  likes: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/likes`,

  gifts: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/gifts`,

  settings: (liveId) =>
    `/api/live/${encodeURIComponent(liveId)}/settings`
};

module.exports = {
  liveRoutes
};
