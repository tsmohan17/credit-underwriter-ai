import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardComponent } from './components/dashboard/dashboard';
import { LoanFormComponent } from './components/loan-form/loan-form';
import { ChatComponent } from './components/chat/chat';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, DashboardComponent, LoanFormComponent, ChatComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  activeTab = signal<'dashboard' | 'form' | 'chat'>('dashboard');

  selectTab(tab: 'dashboard' | 'form' | 'chat'): void {
    this.activeTab.set(tab);
  }
}
