import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {formatIsoDate} from "../../../utils/date-helper";
import {ConfirmDeleteDialogComponent} from "../../confirm-delete-dialog/confirm-delete-dialog.component";

@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-detail.component.html',
  imports: [
    FormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent,
  ],
  standalone: true,
  styleUrls: ['./horse-detail.component.scss']
})
export class HorseDetailComponent implements OnInit {
  bannerError: string | null = null;
  horse: Horse = {
    name: '',
    description: '  ',
    dateOfBirth: new Date(),
    sex: Sex.female,
  };
  parent1: string | undefined;
  parent2: string | undefined;
  imageAvailable = false;
  imagePreview: string | ArrayBuffer | null = null;
  horseForDeletion: Horse | undefined;
  horseId: number | undefined;
  depth: number | undefined;

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
  }

  public get horseBirthDateText(): string {
    return formatIsoDate(this.horse.dateOfBirth);
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.horseId = Number(params.get('id'));
      this.fetchHorseData()
    })
  }

  fetchHorseData() {
    if (this.horseId) {
      this.service.getById(this.horseId).subscribe({
        next: data => {
          this.horse.name = data.name;
          this.horse.description = data.description;
          this.horse.sex = data.sex;
          this.horse.dateOfBirth = new Date(data.dateOfBirth.toString());
          this.horse.owner = data.owner;
          this.horse.parent1Id = data.parent1Id;
          this.horse.parent2Id = data.parent2Id;
          this.depth = 1;

          if(data.parent1Id){
            this.service.getById(data.parent1Id).subscribe({next: data =>
                this.parent1 = data.name
            })
          }
          if(data.parent2Id){
            this.service.getById(data.parent2Id).subscribe({next: data =>
                this.parent2 = data.name
            })
          }

          if (data.image) {
            this.imagePreview = 'data:image/jpeg;base64,' + data.image;
            this.imageAvailable = true
          }

        }, error: error => {
          console.error('Error fetching horses', error);
          this.bannerError = 'Could not fetch horses: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Horses');
        }
      })
    }
  }

  resetHorse() {
    this.horse = {
      name: '',
      description: '',
      dateOfBirth: new Date(),
      sex: Sex.female,
      parent1Id: undefined,
      parent2Id: undefined
    };
    this.parent1 = undefined;
    this.parent2 = undefined;
    this.imageAvailable = false;
    this.imagePreview = null;
    this.horseForDeletion = undefined;
    this.horseId = undefined
  }

  public formatOwnerName(owner: Owner | null | undefined): string {

    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  editHorse() {
    this.router.navigate(['/horses', this.horseId, 'edit'])
  }

  visitHorse(horseId: number) {
    this.resetHorse();
    this.router.navigate(['/horses', horseId])
  }

  viewFamilyTree(id: number){
    if (typeof this.depth === "number") {
      console.log(this.depth)
      this.router.navigate(['/horses', id, 'familytree'], {queryParams: {generations: Math.min(this.depth, 10)}})
    }
  }

  deleteHorse(horse: Horse) {
    console.log("Attempting to delete horse with ID:", horse.id);

    this.service.deleteById(horse.id).subscribe({
      next: (deletedHorse) => {
        console.log("Deleted successfully:", deletedHorse);
        this.router.navigate(["/horses"])
      },
      error: (error) => console.error("Delete failed:", error)
    });

  }

}

