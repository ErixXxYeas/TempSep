import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Horse} from 'src/app/dto/horse';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {ConfirmDeleteDialogComponent} from "../../confirm-delete-dialog/confirm-delete-dialog.component";
import {CommonModule} from "@angular/common";
@Component({
  selector: 'app-horse-detail',
  templateUrl: './horse-family-tree.component.html',
  imports: [
    FormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent,
    CommonModule
  ],
  standalone: true,
  styleUrls: ['./horse-family-tree.component.scss']
})
export class HorseFamilyTreeComponent implements OnInit {
  bannerError: string | null = null;
  horse: Horse | null = null;
  horseForDeletion: Horse | undefined;
  horseId: number | undefined
  allAncestors: Set<Horse> = new Set()
  workingAncestors: Set<Horse> = new Set()
  maxDepth: number | undefined;

  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
  ) {
  }

  public get horseBirthDateText(): string {
    if (!this.horse?.dateOfBirth) {
      return '';
    } else {
      const date = new Date(this.horse.dateOfBirth.toString())
      return date.toLocaleDateString();

    }
  }

  containsHorse(horse: Horse):boolean{
    return this.allAncestors.has(horse);
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      this.horseId = Number(params.get('id'));
    })
    this.route.queryParamMap.subscribe(queryParams => {
      this.maxDepth = Number(queryParams.get('generations')) || 1 ;
    });
    this.fetchHorseData()
  }

  fetchHorseData() {
    if (this.horseId) {
      this.service.getById(this.horseId).subscribe({
        next: data => {
          this.horse = data;
          this.allAncestors.add(data)
          this.workingAncestors.add(data)
          console.log(this.workingAncestors)
          if(this.maxDepth){
            this.getAncestors(data.parent1, this.maxDepth - 1 );
            this.getAncestors(data.parent2, this.maxDepth - 1 );
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

  toggleNode(horse: Horse){
    if (this.allAncestors.has(horse) && this.workingAncestors.has(horse) ){
      this.workingAncestors.delete(horse)
    } else if(this.allAncestors.has(horse)) {
      this.workingAncestors.add(horse);
    }
    if (!horse.parent1 && !horse.parent2){
      this.notification.warning("No Parent")
    }

    if((horse.parent1 && !this.containsHorse(horse.parent1)) || (horse.parent2 && !this.containsHorse(horse.parent2)) ){
      this.notification.warning("Max depth reached")
    }

  }

  isExtended(horse: Horse): boolean{
    return this.workingAncestors.has(horse)
  }

  getAncestors(horse: Horse | undefined, depth: number) {
    if(!horse || depth <= 0){
      return
    }
    depth = depth - 1;
    if(horse){
     this.allAncestors.add(horse)
     this.workingAncestors.add(horse)
    }
    if (horse.parent1) {
      this.getAncestors(horse.parent1, depth);
    }
    if (horse.parent2) {
      this.getAncestors(horse.parent2, depth);
    }
  }

  visitHorse(horseId: number) {
    console.log("hey")
    console.log(horseId)
    this.router.navigate(['/horses', horseId])
  }

  deleteHorse(horse: Horse) {
    console.log("Attempting to delete horse with ID:", horse);
    this.service.deleteById(horse.id).subscribe({
      next: (deletedHorse) => {
        console.log("Deleted successfully:", deletedHorse);
        this.fetchHorseData()
      },
      error: (error) => console.error("Delete failed:", error)
    });

  }

}

