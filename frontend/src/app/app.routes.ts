import {Routes} from '@angular/router';
import {HorseCreateEditComponent, HorseCreateEditMode} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import {HorseEditEditComponent, HorseEditEditMode} from "./component/horse/horse-edit-edit/horse-edit-edit.component";


export const routes: Routes = [
  {path: 'horses', children: [
    {path: '', component: HorseComponent},
    {path: 'create', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.create}},
    {path: ':id/edit', component: HorseEditEditComponent, data: {mode: HorseEditEditMode.edit}},
    //{path: ':id', component: HorseEditEditComponent, data: {mode: HorseEditEditMode.create}}
  ]},
  {path: '**', redirectTo: 'horses'},
];
