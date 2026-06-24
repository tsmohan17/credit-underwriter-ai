import { Component, ElementRef, ViewChild, AfterViewChecked, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoanService } from '../../services/loan.service';

interface Message {
  sender: 'user' | 'bot';
  text: string;
  timestamp: Date;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="chat-container">
      <div class="chat-header">
        <h2 class="chat-title">AI Credit Underwriting Advisor</h2>
        <p class="chat-subtitle">Ask questions about loan eligibility, debt management, and interest rate guidelines.</p>
      </div>

      <div class="chat-messages" #messageContainer>
        <div *ngFor="let msg of messages()" class="message-wrapper" [class.user-wrapper]="msg.sender === 'user'">
          <div class="avatar" [class.user-avatar]="msg.sender === 'user'">
            {{ msg.sender === 'user' ? 'U' : 'AI' }}
          </div>
          <div class="message-bubble" [class.user-bubble]="msg.sender === 'user'">
            <p class="message-text">{{ msg.text }}</p>
            <span class="message-time">{{ msg.timestamp | date:'shortTime' }}</span>
          </div>
        </div>
        <div *ngIf="loading()" class="message-wrapper">
          <div class="avatar">AI</div>
          <div class="message-bubble loading-bubble">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
        </div>
      </div>

      <div class="chat-input-area">
        <input 
          type="text" 
          [(ngModel)]="userInput" 
          (keydown.enter)="sendMessage()" 
          [disabled]="loading()"
          placeholder="Ask a question (e.g. 'How can I improve my DTI ratio?')" 
          class="chat-input"
        />
        <button class="btn btn-primary send-btn" (click)="sendMessage()" [disabled]="loading() || !userInput.trim()">
          Send
        </button>
      </div>
    </div>
  `,
  styles: [`
    .chat-container {
      background-color: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 8px;
      height: 550px;
      display: flex;
      flex-direction: column;
      max-width: 800px;
      margin: 0 auto;
      overflow: hidden;
    }
    .chat-header {
      padding: 16px 20px;
      border-bottom: 1px solid var(--border-color);
      background-color: var(--table-header-bg);
    }
    .chat-title {
      font-size: 1.125rem;
      font-weight: 700;
      color: var(--text-primary);
      margin: 0 0 2px 0;
    }
    .chat-subtitle {
      font-size: 0.8125rem;
      color: var(--text-muted);
      margin: 0;
    }
    .chat-messages {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 16px;
      background-color: var(--panel-sub-bg);
    }
    .message-wrapper {
      display: flex;
      gap: 12px;
      align-items: flex-start;
      max-width: 80%;
      animation: fadeIn 0.2s ease-out;
    }
    .user-wrapper {
      align-self: flex-end;
      flex-direction: row-reverse;
      max-width: 80%;
    }
    .avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background-color: #8b5cf6;
      color: #fff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 0.75rem;
      font-weight: 700;
      flex-shrink: 0;
    }
    .user-avatar {
      background-color: #3b82f6;
    }
    .message-bubble {
      background-color: var(--card-bg);
      border: 1px solid var(--border-color);
      border-radius: 0 12px 12px 12px;
      padding: 10px 14px;
      box-shadow: 0 1px 2px rgba(0,0,0,0.05);
    }
    .user-bubble {
      background-color: #3b82f6;
      border-color: #2563eb;
      color: #fff;
      border-radius: 12px 0 12px 12px;
    }
    .message-text {
      margin: 0 0 4px 0;
      font-size: 0.875rem;
      line-height: 1.45;
      white-space: pre-wrap;
    }
    .message-time {
      font-size: 0.6875rem;
      color: var(--text-muted);
      display: block;
      text-align: right;
    }
    .user-bubble .message-time {
      color: rgba(255,255,255,0.7);
    }
    .chat-input-area {
      padding: 16px;
      border-top: 1px solid var(--border-color);
      display: flex;
      gap: 12px;
      background-color: var(--card-bg);
    }
    .chat-input {
      flex: 1;
      background-color: var(--input-bg);
      border: 1px solid var(--border-color);
      border-radius: 6px;
      padding: 10px 14px;
      color: var(--text-primary);
      font-size: 0.875rem;
    }
    .chat-input:focus {
      outline: none;
      border-color: #3b82f6;
    }
    .send-btn {
      padding: 10px 20px;
    }
    .loading-bubble {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 14px 18px;
    }
    .dot {
      width: 6px;
      height: 6px;
      background-color: var(--text-muted);
      border-radius: 50%;
      display: inline-block;
      animation: bounce 1.4s infinite ease-in-out both;
    }
    .dot:nth-child(1) { animation-delay: -0.32s; }
    .dot:nth-child(2) { animation-delay: -0.16s; }
    
    @keyframes bounce {
      0%, 80%, 100% { transform: scale(0); }
      40% { transform: scale(1.0); }
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(5px); }
      to { opacity: 1; transform: translateY(0); }
    }
  `]
})
export class ChatComponent implements AfterViewChecked {
  @ViewChild('messageContainer') private messageContainer!: ElementRef;

  messages = signal<Message[]>([
    {
      sender: 'bot',
      text: 'Hello! I am your AI Underwriting Advisor. Ask me anything about how credit decisions are made, how to calculate your Debt-to-Income (DTI) ratio, or how to qualify for better rates.',
      timestamp: new Date()
    }
  ]);
  userInput = '';
  loading = signal(false);

  constructor(private loanService: LoanService) {}

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  sendMessage(): void {
    if (!this.userInput.trim() || this.loading()) return;

    const userText = this.userInput;
    this.messages.update(prev => [...prev, {
      sender: 'user',
      text: userText,
      timestamp: new Date()
    }]);
    this.userInput = '';
    this.loading.set(true);

    this.loanService.getAiAdvice(userText).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.messages.update(prev => [...prev, {
          sender: 'bot',
          text: res.response,
          timestamp: new Date()
        }]);
      },
      error: () => {
        this.loading.set(false);
        this.messages.update(prev => [...prev, {
          sender: 'bot',
          text: 'Sorry, I failed to reach the AI engine. Please ensure the underwriting service is running locally on port 8082.',
          timestamp: new Date()
        }]);
      }
    });
  }

  private scrollToBottom(): void {
    try {
      this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
    } catch {}
  }
}
