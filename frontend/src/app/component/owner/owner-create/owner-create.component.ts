import {Component, OnInit} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {map, Observable, of} from 'rxjs';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Owner} from 'src/app/dto/owner';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
import {OwnerService} from 'src/app/service/owner.service';
import {convertFromHorseToCreate, Horse} from "../../../dto/horse";
import {HorseCreateEditMode} from "../../horse/horse-create-edit/horse-create-edit.component";

export enum OwnerCreate {
  create,
  edit
}

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  imports: [
    FormsModule,
    AutocompleteComponent,
  ],
  standalone: true,
  styleUrls: ['./owner-create.component.scss']
})
export class OwnerCreateComponent implements OnInit {
  bannerError: string | null = null;
  mode: OwnerCreate = OwnerCreate.create;
  owner: Owner ={
    firstName: '',
    lastName: '',
    description: ''

  }

  constructor(
    private service: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  public get heading(): string {
    switch (this.mode) {
      case OwnerCreate.create:
        return 'Create New Owner';
      case OwnerCreate.edit:
        return 'Edit Owner';
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case OwnerCreate.create:
        return 'Create';
      case OwnerCreate.edit:
        return 'Change';
      default:
        return '?';
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === OwnerCreate.create;
  }
  private get modeActionFinished(): string {
    switch (this.mode) {
      case OwnerCreate.create:
        return 'created';
      case OwnerCreate.edit:
        return 'changed';
      default:
        return '?';
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchByName(input, 5);


  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
    });
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

  public onSubmit(form: NgForm): void {
    if (form.valid) {
      if (this.owner.description === '') {
        delete this.owner.description;
      }
      let observable: Observable<Owner>;
      switch (this.mode) {
        case OwnerCreate.create:
          observable = this.service.create(this.owner);
          break;
        default:
          console.error('Unknown OwnerCreateMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          this.notification.success(`Owner ${this.owner.firstName + " " + this.owner.lastName}  successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/owners']);
        },
        error: error => {
          console.error('Error creating horse', error);
          this.notification.error(this.errorFormatter.format(error), 'Could Not Create Owner', {
            enableHtml: true,
            timeOut: 10000,
          });
        }
      });
    }
  }
}
