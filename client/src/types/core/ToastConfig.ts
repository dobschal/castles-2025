export interface ToastConfig {
  type: "info" | "success" | "danger" | "warning";
  message?: string;
  messageKey?: string;
  messageParams?: string[];
}
