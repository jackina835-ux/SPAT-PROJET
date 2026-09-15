import client from "./client";

export function listerNotifications() {
  return client.get("/notifications");
}

export function compterNonLues() {
  return client.get("/notifications/non-lues");
}

export function marquerLue(id) {
  return client.patch(`/notifications/${id}/lue`);
}

export function toutMarquerLu() {
  return client.patch("/notifications/tout-lu");
}

export function supprimerNotification(id) {
  return client.delete(`/notifications/${id}`);
}
