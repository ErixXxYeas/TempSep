import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  imports: [RouterLink],
  standalone: true,
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {

}
