import {Component, OnInit} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {map, Observable, of} from 'rxjs';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {convertFromHorseToCreate, Horse, HorseSearch} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {formatIsoDate} from "../../../utils/date-helper";
import {NgForOf} from "@angular/common";

export enum HorseCreateEditMode {
  create,
  edit
}

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  imports: [
    FormsModule,
    AutocompleteComponent,
    NgForOf,
  ],
  standalone: true,
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {
  bannerError: string | null = null;
  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
  };
  parent1: Horse = {name: '', dateOfBirth: new Date(), sex: Sex.female,}
  parent2: Horse = {name: '', dateOfBirth: new Date(), sex: Sex.male,}
  horseBirthDateIsSet = false;
  imageAvailable = false;
  imageFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;
  horseId : number | undefined;
  remainingCharacters : number = 4095;


  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }


  updateRemainingCharacter(){
    this.remainingCharacters = 4095 - (this.horse.description?.length || 0);
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      case HorseCreateEditMode.edit:
        return 'Edit Horse';
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      case HorseCreateEditMode.edit:
        return 'Change';
      default:
        return '?';
    }
  }

  public get horseBirthDateText(): string {
    if (!this.horseBirthDateIsSet) {
      return '';
    } else {
      return formatIsoDate(this.horse.dateOfBirth);
    }
  }

  public set horseBirthDateText(date: string) {
    if (date == null || date === '') {
      this.horseBirthDateIsSet = false;
    } else {
      console.log(this.parent1)
      if (this.horse.parent1Id){
        if (Date.parse(formatIsoDate(this.horse.dateOfBirth)) > Date.parse(formatIsoDate(this.parent1.dateOfBirth))) {
          this.notification.warning("Mother cannot be younger than this horse")
          this.horse.parent1Id = undefined;
          this.parent1 = {name: '', dateOfBirth: new Date(), sex: Sex.female,}
        }
      }
      if (this.horse.parent2Id){
        if (Date.parse(formatIsoDate(this.horse.dateOfBirth)) > Date.parse(formatIsoDate(this.parent2.dateOfBirth))) {
          this.notification.warning("Father cannot be younger than this horse")
          this.horse.parent2Id = undefined;
          this.parent2 = {name: '', dateOfBirth: new Date(), sex: Sex.male,}
        }
      }
      this.horseBirthDateIsSet = true;
      this.horse.dateOfBirth = new Date(date);
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }

  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      case HorseCreateEditMode.edit:
        return 'changed';
      default:
        return '?';
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  parentSuggestions = (parent: Sex) => {
    return (input: string) => this.parentSuggestionsByGender(input,parent)
  };

  parentSuggestionsByGender = (input: string, sex: Sex) => {

    let searchParams: HorseSearch = {
      name: input,
      sex: sex,
      bornBefore: this.horseBirthDateText,
      limit: 5
    }

    return input === ''
      ? of([])
      : this.service.searchByParams(searchParams).pipe(
        map(horses =>
          horses.filter(
          (horse =>
          !this.isAncestor(horse))
          ))
         );
  };

  private isAncestor( potentialParent: Horse | undefined): boolean {
    if(!potentialParent){
      return false
    }
    if (potentialParent.id === this.horseId){
      return true;
    } else {
      let potentialParent1 = undefined;
      let potentialParent2 = undefined;
      if (potentialParent.parent1Id) {
      this.service.getById(potentialParent.parent1Id).subscribe( data => {
        potentialParent1 = data;})
      }
      if (potentialParent.parent2Id) {
        this.service.getById(potentialParent.parent2Id).subscribe( data => {
          potentialParent1 = data;})
      }
      return this.isAncestor(potentialParent1) || this.isAncestor(potentialParent2);
    }
  }

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
    if (!this.modeIsCreate){
      this.fetchHorseData()
    }
  }


  fetchHorseData(){
    this.horseId = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getById(this.horseId).subscribe({
      next: data =>{
        console.log(data)
        this.horse.name = data.name;
        if(data.description){
          this.horse.description = data.description;
          this.updateRemainingCharacter()
        }
        this.horse.sex = data.sex;
        this.horse.dateOfBirth = data.dateOfBirth;


        this.horse.owner = data.owner
        if(data.parent1Id != null){
          this.horse.parent1Id = data.parent1Id
          this.setParents(data.parent1Id,Sex.female)
        }
        if(data.parent2Id){
          this.horse.parent2Id = data.parent2Id
          this.setParents(data.parent2Id,Sex.male)
        }

        this.horseBirthDateIsSet = true;
        if (data.image && data.id) {
          this.service.getHorseImage(data.id).subscribe(url => {
            this.imagePreview = url
            this.imageAvailable = true;
          })
        }
      }, error: error => {
        console.error('Error fetching horses', error);
        this.bannerError = 'Could not fetch horses: ' + error.message;
        const errorMessage = error.status === 0
          ? 'Server Problems'
          : error.message.message;
        this.notification.error(errorMessage, 'Could Not Fetch Horses');
      }
    })
  }

  setParents(parentId: number, sex: Sex){
    if(parentId) {
      if (sex == Sex.female) {
        this.service.getById(parentId).subscribe({next: data =>
            this.parent1 = data
        })
      } else {
        this.service.getById(parentId).subscribe({next: data =>
            this.parent2 = data
        })
      }
    }
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }
  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public formatHorseName(horse: Horse | null | undefined): string {
    return (horse == null)
      ? ''
      : `${horse.name} `;
  }

  public onParentSelected(parent: Sex, horse: Horse){
    if (horse){
      if(parent === Sex.female){
        this.horse.parent1Id = horse.id
        this.parent1 = horse;
      }
      if(parent === Sex.male){
        this.horse.parent2Id = horse.id
        this.parent2 = horse;
      }
    } else {
      if(parent === Sex.female){
        this.horse.parent1Id = undefined
      }
      if(parent === Sex.male){
        this.horse.parent2Id = undefined
      }
    }
  }

  imageUploaded(event: any): void {
    const file = event.target.files[0];
    if(file){
      this.imageAvailable = true;
      this.imageFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      }
      reader.readAsDataURL(file)
    }
  }

  removeImage(){
    this.imageAvailable = false;
    this.imagePreview = null
    this.imageFile = null;
    this.service.removeImageById(this.horseId).subscribe({next: data =>
    console.log(data)
    })
    const fileInput = document.getElementById("image") as HTMLInputElement;
    if(fileInput){
      fileInput.value = "";
    }
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      if (this.horse.description === '') {
        delete this.horse.description;
      }
      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          observable = this.service.create(
            convertFromHorseToCreate(this.horse) , this.imageFile
          );
          break;
           case HorseCreateEditMode.edit:
             observable = this.service.update(
               convertFromHorseToCreate(this.horse), this.imageFile, Number(this.route.snapshot.paramMap.get('id'))
             );
          break;
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          if(this.modeIsCreate){
            this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
            this.router.navigate(['/horses']);
          } else {
            this.notification.success(`Horse ${data.name} successfully ${this.modeActionFinished}.`);
            this.router.navigate(['/horses']);
          }
        },
        error: error => {
          if(this.modeIsCreate){
          console.error('Error creating horse', error);
          this.notification.error(this.errorFormatter.format(error), 'Could Not Create Horse', {
            enableHtml: true,
            timeOut: 10000,
          });
          } else {
            console.error('Error updating horse', error);
            this.notification.error(this.errorFormatter.format(error), 'Could Not Update Horse', {
              enableHtml: true,
              timeOut: 10000,
            });
          }
        }
      });
    }
  }

  protected readonly Sex = Sex;
}
