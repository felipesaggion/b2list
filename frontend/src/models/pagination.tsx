export default interface Pagination {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    content: OrderListingProjection[];
}