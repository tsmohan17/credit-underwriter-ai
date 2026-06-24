import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoanRequest, LoanResponse } from '../models';

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private loanUrl = 'http://localhost:8081/api/loans';
  private underwritingUrl = 'http://localhost:8082/api/underwriting/advise';

  constructor(private http: HttpClient) {}

  submitLoan(request: LoanRequest): Observable<LoanResponse> {
    return this.http.post<LoanResponse>(this.loanUrl, request);
  }

  getAllLoans(): Observable<LoanResponse[]> {
    return this.http.get<LoanResponse[]>(this.loanUrl);
  }

  getLoanById(id: number): Observable<LoanResponse> {
    return this.http.get<LoanResponse>(`${this.loanUrl}/${id}`);
  }

  getAiAdvice(message: string): Observable<{ response: string }> {
    return this.http.post<{ response: string }>(this.underwritingUrl, { message });
  }
}
