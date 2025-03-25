import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { AutocompleteComponent } from 'src/app/component/autocomplete/autocomplete.component';
import { HorseService } from 'src/app/service/horse.service';
import {Horse, HorseSearch} from 'src/app/dto/horse';
import { Owner } from 'src/app/dto/owner';
import { ConfirmDeleteDialogComponent } from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
import {formatIsoDate} from "../../utils/date-helper";
import {CommonModule} from "@angular/common";
import {Sex} from "../../dto/sex";


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
  uniqueOwners: string[] = [];
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseForDeletion: Horse | undefined;
  searchParameters: HorseSearch = {
    name: "",
    description:"",
    dateOfBirth: undefined,
    sex: undefined,
    ownerName: ""
  }

  constructor(
    private service: HorseService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadHorses();
  }

  reloadHorses() {
    this.uniqueOwners = [];
    this.service.searchByParams(this.searchParameters).subscribe({
      next: (data) => {
        this.horses = data
        this.bannerError = null;
        for (let i = 0; i < data.length ; i++){
          const owner = data[i].owner
          const ownerName = owner ? this.ownerName(owner) : '';
          if (ownerName && !this.uniqueOwners.includes(ownerName)) {
            this.uniqueOwners.push(ownerName);
          }
        }

        console.log(data)
      },
      error: (error) => {
        console.error('Error fetching horses', error);
        this.bannerError = 'Could not fetch horses: ' + error.message;
        this.notification.error(error.message, 'Could Not Fetch Horses');
      }
    });
  }
  validateOwnerSuggestion(){
    const validOptions = this.horses
      .filter(h => h.owner)
      .map(h => `${h.owner?.firstName} ${h.owner?.lastName}`);

    if (!validOptions.includes(<string>this.searchParameters.ownerName)) {
      this.searchParameters.ownerName = '';
    } else {
      this.reloadHorses()
    }
  }

  public get horseBirthDateText(): string {
    if (!this.searchParameters.dateOfBirth) {
      return '';
    } else {
      return formatIsoDate(this.searchParameters.dateOfBirth);
    }
  }

  public set horseBirthDateText(date: string) {
    if (date != null && date !== '') {
      this.searchParameters.dateOfBirth = new Date(date);
    } else {
      this.searchParameters.dateOfBirth = undefined;
    }
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }
  dateOfBirthAsLocaleDate(horse: Horse): string {
    const date = new Date(horse.dateOfBirth.toString())
    return date.toLocaleDateString();
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
