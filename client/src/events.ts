import { defineEvent } from "@/core/event";
import { ToastConfig } from "@/types/core/ToastConfig.ts";

export const SHOW_TOAST = defineEvent<ToastConfig>();
