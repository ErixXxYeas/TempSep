import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AutocompleteComponent } from 'src/app/component/autocomplete/autocomplete.component';
import { HorseService } from 'src/app/service/horse.service';
import { Horse } from 'src/app/dto/horse';
import { Owner } from 'src/app/dto/owner';
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
import {formatIsoDate} from "../../utils/date-helper";
import {CommonModule} from "@angular/common";


@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  imports: [
    RouterLink,
    FormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent,
    CommonModule
  ],
  standalone: true,
  styleUrls: ['./horse.component.scss']
})
export class HorseComponent implements OnInit {
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseForDeletion: Horse | undefined;
  searchName: string = '';
  searchDescription: string = '';
  searchDateOfBirth: Date | undefined;
  searchSex: string = '';
  searchOwner: string = '';

  constructor(
    private service: HorseService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadHorses();
  }

  reloadHorses() {
    this.service.getAll().subscribe({
      next: (data) => {
        this.horses = data.filter(horse =>
          (!this.searchName || horse.name.toLowerCase().includes(this.searchName.toLowerCase())) &&
          (!this.searchDescription || (horse.description && horse.description.toLowerCase().includes(this.searchDescription.toLowerCase()))) &&
          (!this.searchDateOfBirth || formatIsoDate(horse.dateOfBirth) === formatIsoDate(this.searchDateOfBirth)) &&
          (!this.searchSex || horse.sex === this.searchSex) &&
          (!this.searchOwner || (horse.owner && this.ownerName(horse.owner).toLowerCase().includes(this.searchOwner.toLowerCase())))
        );
        this.bannerError = null;
        console.log(data)
      },
      error: (error) => {
        console.error('Error fetching horses', error);
        this.bannerError = 'Could not fetch horses: ' + error.message;
        this.notification.error(error.message, 'Could Not Fetch Horses');
      }
    });
  }

  public get horseBirthDateText(): string {
    if (!this.searchDateOfBirth) {
      return '';
    } else {
      return formatIsoDate(this.searchDateOfBirth);
    }
  }

  public set horseBirthDateText(date: string) {
    if (date != null && date !== '') {
      this.searchDateOfBirth = new Date(date);
    } else {
      this.searchDateOfBirth = undefined
    }
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return horse.dateOfBirth.toLocaleDateString();
  }

  deleteHorse(horse: Horse) {
    console.log("Attempting to delete horse with ID:", horse.id);
    this.service.deleteById(horse.id).subscribe({
      next: (deletedHorse) => {
        console.log("Deleted successfully:", deletedHorse);
        this.reloadHorses();
      },
      error: (error) => console.error("Delete failed:", error)
    });


  }



}
