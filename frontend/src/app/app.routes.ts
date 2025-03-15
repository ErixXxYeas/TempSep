import {Routes} from '@angular/router';
import {HorseCreateEditComponent, HorseCreateEditMode} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import {HorseDetailComponent} from "./component/horse/horse-detail/horse-detail.component";
import {OwnerCreate, OwnerCreateComponent} from "./component/horse/owner-create/owner-create.component";


export const routes: Routes = [
  {path: 'horses', children: [
    {path: '', component: HorseComponent},
    {path: 'create/horse', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.create}},
    {path: ':id/edit', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.edit}},
    {path: ':id', component: HorseDetailComponent},
    {path: 'create/owner', component: OwnerCreateComponent, data: {mode: OwnerCreate.create}}
  ]},
  {path: '**', redirectTo: 'horses'},
];
