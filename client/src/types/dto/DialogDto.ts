export interface DialogDto {
  questionKey: string;
  yesButtonKey?: string;
  noButtonKey?: string;
  onYes?: () => void;
}
