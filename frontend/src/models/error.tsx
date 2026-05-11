export interface ErrorResponse {
  status: number;
  code: string;
  message: string;
  details: string[];
  traceId: string;
  timestamp: string | Date;
}