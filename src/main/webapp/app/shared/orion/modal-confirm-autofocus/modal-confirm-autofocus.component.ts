import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'jhi-modal-confirm-autofocus',
    templateUrl: './modal-confirm-autofocus.component.html',
    styles: [],
})
export class ModalConfirmAutofocusComponent implements OnInit {
    title: string;
    text: string;

    constructor(public modal: NgbActiveModal) {}

    ngOnInit() {}
}
