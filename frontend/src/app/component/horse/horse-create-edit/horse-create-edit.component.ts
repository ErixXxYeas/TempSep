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
    parent1: undefined,
    parent2: undefined
  };
  horseBirthDateIsSet = false;
  imageAvailable = false;
  imageFile: File | null = null;
  imagePreview: string | ArrayBuffer | null = null;
  mom: Horse | null = null;
  dad: Horse | null = null;


  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
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
      this.horseBirthDateIsSet = true;
      this.horse.dateOfBirth = new Date(date);
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }
  get sex(): string {
    switch (this.horse.sex) {
      case Sex.male:
        return 'Male';
      case Sex.female:
        return 'Female';
      default:
        return '';
    }
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

  parentSuggestions = (parent: string) => {
    return (input: string) => this.parentSuggestionsByGender(input,parent)
  };

  parentSuggestionsByGender = (input: string, parent: string) => {
    return input === ''
      ? of([])
      : this.service.searchByName(input, 5).pipe(
        map(horses => horses.filter(
          horse => parent === 'mom' ? horse.sex === 'FEMALE' : horse.sex === 'MALE')
        )
      );
  };



  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
    if (!this.modeIsCreate){
      const horseId = Number(this.route.snapshot.paramMap.get('id'));
      this.service.getById(horseId).subscribe({
        next: data =>{
          this.horse.name = data.name;
          this.horse.description = data.description;
          this.horse.sex = data.sex;
          this.horse.dateOfBirth = new Date(data.dateOfBirth.toString());
          this.horseBirthDateIsSet = true;

            this.horse.parent1 = data.parent1

            this.horse.parent2 = data.parent2

            this.horse.owner = data.owner

          console.log(data)
          if (data.image) {
            this.imageFile = this.imageToFile(data.image,"image")
            this.imagePreview = 'data:image/jpeg;base64,' + data.image;
            if (data.image != null) {
              this.imageAvailable = true;
            }
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

  imageToFile(image: string, name: string): File{
    const byteCharacters = atob(image);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) {
      byteNumbers[i] = byteCharacters.charCodeAt(i);
    }
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray] );
    return new File([blob], name );

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

  public onParentSelected(parent: string, horse: Horse){

    if (horse){
      if(parent === "mom"){
        this.horse.parent1 = horse
      }
      if(parent === "dad"){
        this.horse.parent2 = horse
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
          /* case HorseCreateEditMode.edit:
             observable = this.service.update(
               convertFromHorseToCreate(this.horse, this.imageFile), Number(this.route.snapshot.paramMap.get('id'))
             );
          break;
          */
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          console.error('Error creating horse', error);
          this.notification.error(this.errorFormatter.format(error), 'Could Not Create Horse', {
            enableHtml: true,
            timeOut: 10000,
          });
        }
      });
    }
  }
}
