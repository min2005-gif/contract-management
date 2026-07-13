-- Reference to the signed document produced by the digital-signature integration (US5).
ALTER TABLE contract ADD COLUMN signed_document_ref VARCHAR(500);
