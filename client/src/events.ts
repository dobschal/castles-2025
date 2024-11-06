import { defineEvent } from "@/core/event";
import { ToastConfig } from "@/types/core/ToastConfig.ts";
import { type Component } from "vue";
import { MapTileDto } from "@/types/dto/MapTileDto.ts";
import { DialogDto } from "@/types/dto/DialogDto.ts";
import { AudioConfigDto } from "@/types/dto/AudioConfigDto.ts";

export const TOAST = defineEvent<ToastConfig>();
export const ACTION = defineEvent<Component>();
export const DIALOG = defineEvent<DialogDto>();
export const PLAY_AUDIO = defineEvent<AudioConfigDto>();
export const STOP_AUDIO = defineEvent<AudioConfigDto>();

export const MAP_TILE_CLICKED = defineEvent<MapTileDto>();
