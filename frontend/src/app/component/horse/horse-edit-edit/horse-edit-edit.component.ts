import {Component, OnInit} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Horse, convertFromHorseToCreate} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {formatIsoDate} from "../../../utils/date-helper";


export enum HorseEditEditMode {
  create,
  edit
}

@Component({
  selector: 'app-horse-edit-edit',
  templateUrl: './horse-edit-edit.component.html',
  imports: [
    FormsModule,
    AutocompleteComponent,
    FormsModule
  ],
  standalone: true,
  styleUrls: ['./horse-edit-edit.component.scss']
})
export class HorseEditEditComponent implements OnInit {
  bannerError: string | null = null;
  mode: HorseEditEditMode = HorseEditEditMode.create;
  horse: Horse = {
    name: '',
    description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
  };
  horseBirthDateIsSet = true;
  imageFile: File | null = null;
  imagePreview: string | ArrayBuffer | null;

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
      case HorseEditEditMode.create:
        return 'Edit Horse';
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseEditEditMode.create:
        return 'Create';
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
    return this.mode === HorseEditEditMode.create;
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
      case HorseEditEditMode.create:
        return 'created';
      default:
        return '?';
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  ngOnInit(): void {
  this.loadHorseInfo();
  }



  loadHorseInfo(){
    const horseId = Number(this.route.snapshot.paramMap.get('id'));
    this.service.getById(horseId).subscribe({
      next: data =>{
        this.horse.name = data.name;
        this.horse.description = data.description;
        this.horse.sex = data.sex;
        this.horse.dateOfBirth = new Date(data.dateOfBirth.toString());



        //Todo: display image


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

  imageUploaded(event: any): void {
    const file = event.target.files[0];
    if(file){
      this.imageFile = file;
      const reader = new FileReader();
      reader.onload = () => {
        this.imagePreview = reader.result;
      }
      reader.readAsDataURL(file)
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
        case HorseEditEditMode.create:
          observable = this.service.update(
            convertFromHorseToCreate(this.horse, this.imageFile), Number(this.route.snapshot.paramMap.get('id'))
          );
          break;
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
