import { Component, OnInit, OnDestroy, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoanService } from '../../services/loan.service';
import { LoanResponse } from '../../models';
import { Subscription, interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <div>
          <h2 class="view-title">Underwriting Applications</h2>
          <p class="view-subtitle">Real-time status of loan evaluations and automated AI risk profiles.</p>
        </div>
        <button class="btn btn-primary" (click)="onCreateNew.emit()">
          <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" style="width: 16px; height: 16px; margin-right: 6px;"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15" /></svg>
          New Application
        </button>
      </div>

      <div class="table-card">
        <div class="table-wrapper">
          <table class="data-table">
            <thead>
              <tr>
                <th>App ID</th>
                <th>Applicant Name</th>
                <th>PAN Card</th>
                <th>Income</th>
                <th>Requested Amount</th>
                <th>Status</th>
                <th>Interest Rate</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let loan of loans()" [class.selected]="selectedLoan()?.id === loan.id" (click)="selectLoan(loan)">
                <td class="font-mono">#{{ loan.id }}</td>
                <td class="font-semibold">{{ loan.customerName }}</td>
                <td class="font-mono text-xs">{{ loan.panNumber }}</td>
                <td>\${{ loan.monthlyIncome | number:'1.2-2' }}/mo</td>
                <td class="font-semibold">\${{ loan.loanAmount | number:'1.2-2' }}</td>
                <td>
                  <span class="status-badge" [ngClass]="'status-' + loan.status.toLowerCase()">
                    {{ loan.status }}
                  </span>
                </td>
                <td class="font-semibold text-center">
                  {{ loan.interestRate ? (loan.interestRate | number:'1.1-2') + '%' : '—' }}
                </td>
                <td>
                  <button class="btn btn-sm btn-outline" (click)="selectLoan(loan); $event.stopPropagation();">
                    Analyze
                  </button>
                </td>
              </tr>
              <tr *ngIf="loans().length === 0">
                <td colspan="8" class="empty-state">
                  <p>No loan applications found. Submit a new application to trigger the underwriting pipeline.</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Detail Panel (Risk Assessment) -->
      <div class="detail-panel" *ngIf="selectedLoan()" [class.approved]="selectedLoan()?.status === 'APPROVED'" [class.declined]="selectedLoan()?.status === 'DECLINED'">
        <div class="detail-header">
          <div>
            <h3 class="detail-title">Risk Analysis: {{ selectedLoan()?.customerName }}</h3>
            <p class="detail-subtitle">Application ID: #{{ selectedLoan()?.id }} · Evaluated via Spring AI</p>
          </div>
          <button class="close-btn" (click)="selectedLoan.set(null)">&times;</button>
        </div>

        <div class="detail-grid">
          <!-- Credit Score Circle -->
          <div class="score-card">
            <div class="score-dial">
              <div class="score-value">{{ getScoreDisplay(selectedLoan()) }}</div>
              <div class="score-label">Credit Rating</div>
            </div>
            <p class="score-meta">
              Interest Rate: <strong>{{ selectedLoan()?.interestRate ? (selectedLoan()?.interestRate | number:'1.1-2') + '%' : 'N/A' }}</strong>
            </p>
          </div>

          <!-- Decision Details -->
          <div class="report-card">
            <div class="report-header">
              <span class="report-status-lbl">Decision status:</span>
              <span class="status-badge" [ngClass]="'status-' + selectedLoan()?.status?.toLowerCase()">
                {{ selectedLoan()?.status }}
              </span>
            </div>
            <h4 class="report-section-title">AI Underwriter Findings</h4>
            <p class="report-text">
              {{ selectedLoan()?.aiDecisionSummary || 'Risk assessment in progress... Processing event via Apache Kafka broker.' }}
            </p>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-container {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }
    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .view-title {
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0 0 4px 0;
    }
    .view-subtitle {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin: 0;
    }
    .table-card {
      background-color: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      overflow: hidden;
    }
    .table-wrapper {
      overflow-x: auto;
    }
    .data-table {
      width: 100%;
      border-collapse: collapse;
      text-align: left;
      font-size: 0.875rem;
    }
    .data-table th {
      background-color: var(--table-header-bg);
      color: var(--text-muted);
      font-weight: 600;
      padding: 12px 16px;
      border-bottom: 1px solid var(--border-color);
      text-transform: uppercase;
      font-size: 0.75rem;
      letter-spacing: 0.05em;
    }
    .data-table td {
      padding: 14px 16px;
      border-bottom: 1px solid var(--border-color);
      color: var(--text-secondary);
    }
    .data-table tr {
      cursor: pointer;
      transition: background-color 0.2s ease;
    }
    .data-table tr:hover {
      background-color: var(--table-hover-bg);
    }
    .data-table tr.selected {
      background-color: var(--table-selected-bg);
    }
    .font-mono {
      font-family: monospace;
    }
    .font-semibold {
      font-weight: 600;
    }
    .text-center {
      text-align: center;
    }
    .text-xs {
      font-size: 0.75rem;
    }
    .empty-state {
      text-align: center;
      padding: 40px !important;
      color: var(--text-muted);
    }
    .status-badge {
      display: inline-flex;
      align-items: center;
      font-size: 0.75rem;
      font-weight: 600;
      padding: 4px 8px;
      border-radius: 12px;
      text-transform: uppercase;
    }
    .status-submitted {
      background-color: rgba(59, 130, 246, 0.1);
      color: #3b82f6;
    }
    .status-under_review {
      background-color: rgba(245, 158, 11, 0.1);
      color: #f59e0b;
    }
    .status-approved {
      background-color: rgba(16, 185, 129, 0.1);
      color: #10b981;
    }
    .status-declined {
      background-color: rgba(244, 63, 94, 0.1);
      color: #f43f5e;
    }
    .detail-panel {
      background-color: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 20px;
      margin-top: 10px;
      animation: slideIn 0.3s ease-out;
    }
    .detail-panel.approved {
      border-left: 4px solid #10b981;
    }
    .detail-panel.declined {
      border-left: 4px solid #f43f5e;
    }
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 20px;
      border-bottom: 1px solid var(--border-color);
      padding-bottom: 12px;
    }
    .detail-title {
      font-size: 1.125rem;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0 0 2px 0;
    }
    .detail-subtitle {
      font-size: 0.75rem;
      color: var(--text-muted);
      margin: 0;
    }
    .close-btn {
      background: none;
      border: none;
      font-size: 1.5rem;
      color: var(--text-muted);
      cursor: pointer;
      line-height: 1;
    }
    .close-btn:hover {
      color: var(--text-primary);
    }
    .detail-grid {
      display: grid;
      grid-template-columns: 200px 1fr;
      gap: 24px;
    }
    @media (max-width: 768px) {
      .detail-grid {
        grid-template-columns: 1fr;
      }
    }
    .score-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      background-color: var(--panel-sub-bg);
      border-radius: 8px;
      padding: 20px;
      text-align: center;
      border: 1px solid var(--border-color);
    }
    .score-dial {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      border: 6px solid var(--border-color);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      margin-bottom: 12px;
    }
    .detail-panel.approved .score-dial {
      border-color: #10b981;
    }
    .detail-panel.declined .score-dial {
      border-color: #f43f5e;
    }
    .score-value {
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--text-primary);
    }
    .score-label {
      font-size: 0.625rem;
      color: var(--text-muted);
      text-transform: uppercase;
      font-weight: 600;
    }
    .score-meta {
      font-size: 0.8125rem;
      color: var(--text-secondary);
      margin: 0;
    }
    .report-card {
      display: flex;
      flex-direction: column;
      gap: 12px;
    }
    .report-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 0.8125rem;
      color: var(--text-secondary);
    }
    .report-status-lbl {
      color: var(--text-muted);
    }
    .report-section-title {
      font-size: 0.875rem;
      font-weight: 600;
      color: var(--text-primary);
      margin: 0;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .report-text {
      font-size: 0.875rem;
      line-height: 1.5;
      color: var(--text-secondary);
      margin: 0;
      white-space: pre-line;
    }
    @keyframes slideIn {
      from { transform: translateY(10px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
  `]
})
export class DashboardComponent implements OnInit, OnDestroy {
  @Output() onCreateNew = new EventEmitter<void>();

  loans = signal<LoanResponse[]>([]);
  selectedLoan = signal<LoanResponse | null>(null);
  private pollingSub?: Subscription;

  constructor(private loanService: LoanService) {}

  ngOnInit(): void {
    // Poll loan applications every 3 seconds to show real-time Kafka status updates!
    this.pollingSub = interval(3000)
      .pipe(
        startWith(0),
        switchMap(() => this.loanService.getAllLoans())
      )
      .subscribe({
        next: (data) => {
          this.loans.set(data.sort((a, b) => b.id - a.id));
          // Auto-update selected loan if its status changed
          const currentSelected = this.selectedLoan();
          if (currentSelected) {
            const updated = this.loans().find(l => l.id === currentSelected.id);
            if (updated) this.selectedLoan.set(updated);
          }
        },
        error: (err) => console.error('Error polling loan list: ', err)
      });
  }

  ngOnDestroy(): void {
    if (this.pollingSub) {
      this.pollingSub.unsubscribe();
    }
  }

  selectLoan(loan: LoanResponse): void {
    this.selectedLoan.set(loan);
  }

  getScoreDisplay(loan: LoanResponse | null | undefined): string {
    if (!loan) return '...';
    if (loan.status === 'SUBMITTED') return '...';
    // Mock rating/score since score itself is in the DB decision table
    if (loan.status === 'APPROVED') {
      return loan.interestRate && loan.interestRate < 10.0 ? '780' : '690';
    }
    return '450';
  }
}
