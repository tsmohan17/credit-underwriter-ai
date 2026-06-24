export type LoanStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'DECLINED';

export interface LoanRequest {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  panNumber: string;
  monthlyIncome: number;
  existingEmi: number;
  loanAmount: number;
  tenureMonths: number;
}

export interface LoanResponse {
  id: number;
  customerName: string;
  email: string;
  panNumber: string;
  monthlyIncome: number;
  existingEmi: number;
  loanAmount: number;
  tenureMonths: number;
  status: LoanStatus;
  interestRate?: number;
  aiDecisionSummary?: string;
  createdAt: string;
  updatedAt: string;
}
