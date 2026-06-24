import { Component, Output, EventEmitter, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LoanService } from '../../services/loan.service';
import { LoanRequest } from '../../models';

@Component({
  selector: 'app-loan-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="form-container">
      <div class="form-header">
        <h2 class="form-title">Apply for a New Loan</h2>
        <p class="form-subtitle">Fill in the applicant details to submit a new credit underwriting request.</p>
      </div>

      <form [formGroup]="loanForm" (ngSubmit)="onSubmit()" class="loan-form">
        <div class="form-grid">
          <!-- First Name -->
          <div class="form-group">
            <label for="firstName">First Name</label>
            <input id="firstName" type="text" formControlName="firstName" class="form-control" placeholder="John" />
            <div class="error-msg" *ngIf="isInvalid('firstName')">
              First name is required.
            </div>
          </div>

          <!-- Last Name -->
          <div class="form-group">
            <label for="lastName">Last Name</label>
            <input id="lastName" type="text" formControlName="lastName" class="form-control" placeholder="Doe" />
            <div class="error-msg" *ngIf="isInvalid('lastName')">
              Last name is required.
            </div>
          </div>

          <!-- Email -->
          <div class="form-group">
            <label for="email">Email Address</label>
            <input id="email" type="email" formControlName="email" class="form-control" placeholder="john.doe@example.com" />
            <div class="error-msg" *ngIf="isInvalid('email')">
              Please enter a valid email address.
            </div>
          </div>

          <!-- Phone Number -->
          <div class="form-group">
            <label for="phoneNumber">Phone Number (10 digits)</label>
            <input id="phoneNumber" type="text" formControlName="phoneNumber" class="form-control" placeholder="9876543210" />
            <div class="error-msg" *ngIf="isInvalid('phoneNumber')">
              Phone number must be exactly 10 digits.
            </div>
          </div>

          <!-- PAN Number -->
          <div class="form-group">
            <label for="panNumber">PAN Number (Format: ABCDE1234F)</label>
            <input id="panNumber" type="text" formControlName="panNumber" class="form-control" placeholder="ABCDE1234F" style="text-transform: uppercase;" />
            <div class="error-msg" *ngIf="isInvalid('panNumber')">
              Invalid PAN card format (e.g. ABCDE1234F).
            </div>
          </div>

          <!-- Monthly Income -->
          <div class="form-group">
            <label for="monthlyIncome">Net Monthly Income ($)</label>
            <input id="monthlyIncome" type="number" formControlName="monthlyIncome" class="form-control" placeholder="5000" />
            <div class="error-msg" *ngIf="isInvalid('monthlyIncome')">
              Monthly income must be positive.
            </div>
          </div>

          <!-- Existing EMI -->
          <div class="form-group">
            <label for="existingEmi">Existing Monthly Debt EMIs ($)</label>
            <input id="existingEmi" type="number" formControlName="existingEmi" class="form-control" placeholder="800" />
            <div class="error-msg" *ngIf="isInvalid('existingEmi')">
              Existing EMI must be 0 or positive.
            </div>
          </div>

          <!-- Loan Amount -->
          <div class="form-group">
            <label for="loanAmount">Requested Loan Amount ($)</label>
            <input id="loanAmount" type="number" formControlName="loanAmount" class="form-control" placeholder="25000" />
            <div class="error-msg" *ngIf="isInvalid('loanAmount')">
              Loan amount must be positive.
            </div>
          </div>

          <!-- Tenure -->
          <div class="form-group">
            <label for="tenureMonths">Loan Tenure (Months)</label>
            <input id="tenureMonths" type="number" formControlName="tenureMonths" class="form-control" placeholder="36" />
            <div class="error-msg" *ngIf="isInvalid('tenureMonths')">
              Tenure must be between 3 and 360 months.
            </div>
          </div>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-outline" (click)="onCancel.emit()">Cancel</button>
          <button type="submit" class="btn btn-primary" [disabled]="loanForm.invalid || submitting()">
            {{ submitting() ? 'Submitting...' : 'Submit Application' }}
          </button>
        </div>

        <div class="alert alert-danger" *ngIf="submitError()">
          {{ submitError() }}
        </div>
      </form>
    </div>
  `,
  styles: [`
    .form-container {
      background-color: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      padding: 24px;
      max-width: 800px;
      margin: 0 auto;
    }
    .form-header {
      margin-bottom: 24px;
      border-bottom: 1px solid var(--border-color);
      padding-bottom: 14px;
    }
    .form-title {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0 0 4px 0;
    }
    .form-subtitle {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin: 0;
    }
    .form-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 16px;
    }
    @media (max-width: 600px) {
      .form-grid {
        grid-template-columns: 1fr;
      }
    }
    .form-group {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .form-group label {
      font-size: 0.8125rem;
      font-weight: 600;
      color: var(--text-secondary);
    }
    .form-control {
      background-color: var(--input-bg);
      border: 1px solid var(--border-color);
      border-radius: 6px;
      padding: 10px 12px;
      color: var(--text-primary);
      font-size: 0.875rem;
      transition: border-color 0.2s ease, box-shadow 0.2s ease;
    }
    .form-control:focus {
      outline: none;
      border-color: #3b82f6;
      box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.15);
    }
    .error-msg {
      font-size: 0.75rem;
      color: #f43f5e;
      margin-top: 2px;
    }
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
      border-top: 1px solid var(--border-color);
      padding-top: 18px;
    }
    .alert-danger {
      background-color: rgba(244, 63, 94, 0.1);
      color: #f43f5e;
      border: 1px solid rgba(244, 63, 94, 0.2);
      border-radius: 6px;
      padding: 12px;
      margin-top: 16px;
      font-size: 0.875rem;
    }
  `]
})
export class LoanFormComponent {
  @Output() onSubmitSuccess = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();

  loanForm: FormGroup;
  submitting = signal(false);
  submitError = signal<string | null>(null);

  constructor(private fb: FormBuilder, private loanService: LoanService) {
    this.loanForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      panNumber: ['', [Validators.required, Validators.pattern('^[A-Z]{5}[0-9]{4}[A-Z]{1}$')]],
      monthlyIncome: [null, [Validators.required, Validators.min(1)]],
      existingEmi: [null, [Validators.required, Validators.min(0)]],
      loanAmount: [null, [Validators.required, Validators.min(1)]],
      tenureMonths: [null, [Validators.required, Validators.min(3), Validators.max(360)]]
    });
  }

  isInvalid(fieldName: string): boolean {
    const control = this.loanForm.get(fieldName);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  onSubmit(): void {
    if (this.loanForm.invalid) return;

    this.submitting.set(true);
    this.submitError.set(null);

    const payload: LoanRequest = {
      ...this.loanForm.value,
      panNumber: this.loanForm.value.panNumber.toUpperCase()
    };

    this.loanService.submitLoan(payload).subscribe({
      next: () => {
        this.submitting.set(false);
        this.loanForm.reset();
        this.onSubmitSuccess.emit();
      },
      error: (err) => {
        this.submitting.set(false);
        this.submitError.set(err.error?.error || 'Failed to submit application. Ensure the backend services are running.');
      }
    });
  }
}
