import {Component, OnInit} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {map, Observable, of} from 'rxjs';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Horse, convertFromHorseToCreate} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
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
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
    parent1: undefined,
    parent2: undefined
  };
  imageAvailable = false;
  imagePreview: string | ArrayBuffer | null = null;
  horseForDeletion: Horse | undefined;
  horseId: number | undefined

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
    this.route.paramMap.subscribe((params) =>
    {
      this.horseId = Number(params.get('id'));
      this.fetchHorseData()
    })
  }

  fetchHorseData(){
    if(this.horseId){
    this.service.getById(this.horseId).subscribe({
      next: data =>{
        this.horse.name = data.name;
        this.horse.description = data.description;
        this.horse.sex = data.sex;
        this.horse.dateOfBirth = new Date(data.dateOfBirth.toString());
        if (data.parent1){
          this.horse.parent1 = data.parent1
        }
        if (data.parent2){
          this.horse.parent2 = data.parent2
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

  resetHorse(){
    this.horse = {
      name: '',
      description: '',
      dateOfBirth: new Date(),
      sex: Sex.female,
      parent1: undefined,
      parent2: undefined
    };
    this.imageAvailable = false;
    this.imagePreview = null;
    this.horseForDeletion =  undefined;
    this.horseId = undefined
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  editHorse(){
  this.router.navigate(['/horses', this.horseId , 'edit'])
  }

  visitHorse( horseId: number){
    console.log("hey")
    console.log(horseId)
    this.resetHorse();
    this.router.navigate(['/horses', horseId ])
  }

  deleteHorse(horse: Horse) {
    console.log("Attempting to delete horse with ID:", this.horseId);

    this.service.deleteById(this.horseId).subscribe({
      next: (deletedHorse) => {
        console.log("Deleted successfully:", deletedHorse);
        this.router.navigate(["/horses"])
      },
      error: (error) => console.error("Delete failed:", error)
    });

  }

}

