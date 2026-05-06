import React, { useEffect, useState, useCallback } from 'react';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    Paper, IconButton, Tooltip, Chip, TablePagination, Typography, Box,
    Grid,
    TextField,
    MenuItem,
    Button,
    InputAdornment,
    Backdrop,
    CircularProgress
} from '@mui/material';
import { Visibility, Cancel, ShoppingCart, FilterList, Clear, Search } from '@mui/icons-material';

import { cancelOrder, getOrdersPaginated } from '../../services/order-service';
import type OrderFilters from '../../models/order-filters';
import { formatISO, isValid } from 'date-fns';
import type Pagination from '../../models/pagination';
import type OrderListingProjection from '../../models/order-listing-projection';
import { useNavigate } from 'react-router-dom';


const Orders: React.FC = () => {
    const navigate = useNavigate()

    const [page, setPage] = useState<number>(0);
    const [rowsPerPage, setRowsPerPage] = useState<number>(5);
    const [isLoading, setIsLoading] = useState<boolean>(true);

    const [pagination, setPagination] = useState<Pagination>({
        totalElements: 0,
        size: 5,
        page: 0,
        totalPages: 0,
        content: [],
    });

    const [filters, setFilters] = useState<OrderFilters>({
        status: '',
        buyerRef: '',
        startDate: new Date(),
        endDate: new Date(),
    });

    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);

    const formatDate = (dateString: string) =>
        new Date(dateString).toLocaleDateString('pt-BR', { hour: '2-digit', minute: '2-digit' });


    const handleChangePage = (_: unknown, newPage: number) => {
        setPage(newPage);
    };

    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRowsPerPage(parseInt(event.target.value, 10)); // Corrigido para base 10
        setPage(0);
    };


    const handleFilterChange = (field: keyof OrderFilters) => (
        event: React.ChangeEvent<HTMLInputElement>
    ) => {
        let value = event.target.value;

        // Se for um campo de data, converte o valor "yyyy-MM-dd" para ISO completo
        if (event.target.type === 'date' && value) {
            const dateObj = new Date(value + 'T12:00:00'); // T12 evita quebra de fuso horário
            if (isValid(dateObj)) {
                value = formatISO(dateObj);
            }
        }
        setFilters((prev) => ({ ...prev, [field]: value }));
    };

    const handleClear = () => {
        setFilters({
            status: '',
            buyerRef: '',
            startDate: new Date(),
            endDate: new Date()
        });
        setPage(0);
    };

    const loadTable = useCallback(() => {
        setIsLoading(true);
        getOrdersPaginated(rowsPerPage, page, filters)
            .then(data => {
                setIsLoading(false);
                setPagination(data as Pagination)}
            )
            .catch(error => {
                setIsLoading(false);
                console.error("Erro ao buscar pedidos:", error)
            });
    }, [rowsPerPage, page, filters]);

    useEffect(() => {
        loadTable();
    }, [page, rowsPerPage]);

    const handleSearch = () => {
        setPage(0);
        loadTable();
    };

    const handleCancelOnClick = (order: OrderListingProjection) => {
        const response = confirm(`Deseja cancelar o pedido '${order.externalReference}' ?`)
        if (order.status === 'CANCELLED') {
            alert(`Pedido '${order.externalReference}' não pode ser cancelado`);
            return;
        }
        if (response) {
            cancelOrder(order.externalReference)
                .then(() => {
                    alert(`Pedido '${order.externalReference}' cancelado com sucesso.`)
                    loadTable();
                })
                .catch(error => {
                    console.error("Erro ao cancelar pedido:", error)
                    alert("Erro ao cancelar pedido.")
                });

        }
    }

    const handleOrderDetailsOnClick = (externalReference: string) => {
        navigate(`/orders/details/${externalReference}`);
    }

    if (isLoading) {
        return <Backdrop
            sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
            open={isLoading}
        >
            <CircularProgress color="inherit" size={60} />

            <Typography variant="h6">
                Carregando pedidos...
            </Typography>
        </Backdrop>
    } else {
        return (
            <Box sx={{ width: '100%', p: 3 }}>
                <Paper elevation={2} sx={{ p: 2, mb: 3, borderRadius: 2 }}>
                    <Grid container spacing={2} sx={{ alignItems: 'center' }}>
                        <Grid size={{ xs: 12, sm: 3 }}>
                            <TextField
                                fullWidth select label="Status" value={filters.status}
                                onChange={handleFilterChange('status')} size="small"
                            >
                                <MenuItem value="">Todos</MenuItem>
                                <MenuItem value="COMPLETED">Completado</MenuItem>
                                <MenuItem value="PENDING">Pendente</MenuItem>
                                <MenuItem value="CANCELLED">Cancelado</MenuItem>
                            </TextField>
                        </Grid>

                        <Grid size={{ xs: 12, sm: 3 }}>
                            <TextField
                                fullWidth label="Ref. Comprador" value={filters.buyerRef}
                                onChange={handleFilterChange('buyerRef')} size="small"
                                slotProps={{
                                    input: {
                                        startAdornment: (
                                            <InputAdornment position="start">
                                                <Search fontSize="small" />
                                            </InputAdornment>
                                        ),
                                    },
                                }}
                            />
                        </Grid>

                        <Grid size={{ xs: 12, sm: 2 }}>
                            <TextField
                                fullWidth label="Início" type="date"
                                // Mostra apenas yyyy-MM-dd no input nativo
                                value={filters.startDate ? filters.startDate.toString().substring(0, 10) : ''}
                                onChange={handleFilterChange('startDate')}
                                size="small"
                                slotProps={{
                                    inputLabel: {
                                        shrink: true,
                                    },
                                }}
                            />
                        </Grid>

                        <Grid size={{ xs: 12, sm: 2 }}>
                            <TextField
                                fullWidth label="Fim" type="date"
                                value={filters.endDate ? filters.endDate.toString().substring(0, 10) : ''}
                                onChange={handleFilterChange('endDate')}
                                size="small"
                                slotProps={{
                                    inputLabel: {
                                        shrink: true,
                                    },
                                }}
                            />
                        </Grid>

                        <Grid size={{ xs: 12, sm: 2 }} sx={{ display: 'flex', gap: 1 }}>
                            <Button
                                fullWidth variant="contained" onClick={handleSearch}
                                startIcon={<FilterList />}
                            >
                                Filtrar
                            </Button>
                            <Button
                                variant="outlined" color="inherit" onClick={handleClear}
                                sx={{ minWidth: '40px' }}
                            >
                                <Clear fontSize="small" />
                            </Button>
                        </Grid>
                    </Grid>
                </Paper>

                <Typography variant="h5" sx={{ mb: 3, fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: 1 }}>
                    <ShoppingCart color="primary" /> Listagem de Pedidos
                </Typography>

                <TableContainer component={Paper} elevation={4} sx={{ borderRadius: 2 }}>
                    <Table sx={{ minWidth: 1100 }} size="small">
                        <TableHead>
                            <TableRow sx={{ backgroundColor: 'action.selected' }}>
                                <TableCell>Referência</TableCell>
                                <TableCell>Data</TableCell>
                                <TableCell>Comprador</TableCell>
                                <TableCell>Vendedor</TableCell>
                                <TableCell>Status</TableCell>
                                <TableCell align="right">Itens</TableCell>
                                <TableCell align="right">Total</TableCell>
                                <TableCell align="center">Ações</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {pagination.content.map((order) => (
                                <TableRow key={order.orderId} hover>
                                    <TableCell sx={{ fontWeight: 'bold' }}>{order.externalReference}</TableCell>
                                    <TableCell>{formatDate(order.createdAt)}</TableCell>
                                    <TableCell>{order.buyerName}</TableCell>
                                    <TableCell>{order.sellerName}</TableCell>
                                    <TableCell>
                                        <Chip
                                            label={order.status.toUpperCase()}
                                            size="small"
                                            color={order.status === 'CANCELLED' ? 'error' : 'success'}
                                            variant="outlined"
                                        />
                                    </TableCell>
                                    <TableCell align="right">{order.itemCount}</TableCell>
                                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>
                                        {formatCurrency(order.total)}
                                    </TableCell>
                                    <TableCell align="center">
                                        <Box sx={{ display: 'flex', justifyContent: 'center' }}>
                                            <Tooltip title="Detalhes" onClick={() => handleOrderDetailsOnClick(order.externalReference)}>
                                                <IconButton size="small" color="primary">
                                                    <Visibility fontSize="small" />
                                                </IconButton>
                                            </Tooltip>
                                            <Tooltip title="Cancelar" onClick={() => handleCancelOnClick(order)}>
                                                <IconButton
                                                    size="small" color="error"
                                                >
                                                    <Cancel fontSize="small" />
                                                </IconButton>
                                            </Tooltip>
                                        </Box>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>

                    <TablePagination
                        component="div"
                        // Conecta diretamente ao que o backend retornou
                        count={pagination.totalElements}
                        rowsPerPage={rowsPerPage}
                        page={page}
                        onPageChange={handleChangePage}
                        onRowsPerPageChange={handleChangeRowsPerPage}
                        labelRowsPerPage="Linhas:"
                        rowsPerPageOptions={[2, 5, 10, 25]}
                    />
                </TableContainer>
            </Box>
        );
    }
};

export default Orders;