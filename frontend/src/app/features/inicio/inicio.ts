import { Component, inject } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-inicio',
  templateUrl: './inicio.html'
})
export class Inicio {
  protected readonly auth = inject(AuthService);
}