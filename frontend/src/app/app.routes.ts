import {Routes} from '@angular/router';
import {HorseCreateEditComponent, HorseCreateEditMode} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import {HorseDetailComponent} from "./component/horse/horse-detail/horse-detail.component";
import {OwnerCreate, OwnerCreateComponent} from "./component/owner/owner-create/owner-create.component";
import {OwnerComponent} from "./component/owner/owner.component";


export const routes: Routes = [
  {path: 'horses', children: [
    {path: '', component: HorseComponent},
    {path: 'create', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.create}},
    {path: ':id/edit', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.edit}},
    {path: ':id', component: HorseDetailComponent},

  ]},
  {path: 'owners', children: [
      {path: '', component: OwnerComponent},
      {path: 'create', component: OwnerCreateComponent, data: {mode: OwnerCreate.create}}
    ]},
  {path: '**', redirectTo: 'horses'},
];
