import { defineEvent } from "@/core/event";
import { ToastConfig } from "@/types/core/ToastConfig.ts";
import { type Component } from "vue";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { DialogDto } from "@/types/dto/DialogDto.ts";

export const SHOW_TOAST = defineEvent<ToastConfig>();
export const ACTION = defineEvent<Component>();
export const CLICKED_MAP_TILE = defineEvent<MapTileDto>();
export const LOADED_MAP_TILES = defineEvent<void>();
export const DIALOG = defineEvent<DialogDto>();
