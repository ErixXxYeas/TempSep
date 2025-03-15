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
import {OwnerService} from "../../service/owner.service";


@Component({
  selector: 'app-horse',
  templateUrl: './owner.component.html',
  imports: [
    RouterLink,
    FormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent
  ],
  standalone: true,
  styleUrls: ['./owner.component.scss']
})
export class OwnerComponent implements OnInit {
  owners: Owner[] = [];
  bannerError: string | null = null;
  searchDescription: string = '';
  searchOwner: string = '';
  ownerForDeletion: Owner | undefined;

  constructor(
    private service: OwnerService,
    private notification: ToastrService,
  ) { }

  ngOnInit(): void {
    this.reloadOwners();
  }

  reloadOwners() {
    this.service.searchByName().subscribe({
      next: (data) => {
        this.owners = data;
        this.bannerError = null;
      },
      error: (error) => {
        console.error('Error fetching owners', error);
        this.bannerError = 'Could not fetch owners: ' + error.message;
        this.notification.error(error.message, 'Could Not Fetch owners');
      }
    });
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }


  deleteOwner(owner: Owner) {
    console.log("Attempting to delete horse with ID:", owner.id);
    this.service.deleteById(owner.id).subscribe({
      next: (deletedHorse) => {
        console.log("Deleted successfully:", deletedHorse);
        this.reloadOwners();
      },
      error: (error) => console.error("Delete failed:", error)
    });

  }


}
