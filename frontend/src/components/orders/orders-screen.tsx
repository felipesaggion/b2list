import React, { useEffect, useState, useCallback } from 'react';
import {
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    Paper, IconButton, Chip, TablePagination, Typography, Box,
    Grid,
    Card,
    CardContent,
    Divider,
    Stack,
    TextField,
    MenuItem,
    Button,
    InputAdornment,
    Backdrop,
    CircularProgress,
    useMediaQuery,
    useTheme
} from '@mui/material';
import { Visibility, Cancel, FilterList, Clear, Search } from '@mui/icons-material';

import { cancelOrder, getOrdersPaginated } from '../../services/order-service';
import type OrderFilters from '../../models/order-filters';
import { formatISO, isValid } from 'date-fns';
import type Pagination from '../../models/pagination';
import type OrderListingProjection from '../../models/order-listing-projection';
import { useNavigate } from 'react-router-dom';

const Orders: React.FC = () => {
    const navigate = useNavigate();
    const theme = useTheme();

    // Detecta se a tela é menor que 600px (celular)
    const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

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

    const handleChangePage = (_: unknown, newPage: number) => setPage(newPage);

    const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    const handleFilterChange = (field: keyof OrderFilters) => (
        event: React.ChangeEvent<HTMLInputElement>
    ) => {
        let value = event.target.value;
        if (event.target.type === 'date' && value) {
            const dateObj = new Date(value + 'T12:00:00');
            if (isValid(dateObj)) {
                value = formatISO(dateObj);
            }
        }
        setFilters((prev) => ({ ...prev, [field]: value }));
    };

    const handleClear = () => {
        setFilters({ status: '', buyerRef: '', startDate: new Date(), endDate: new Date() });
        setPage(0);
    };

    const loadTable = useCallback(() => {
        setIsLoading(true);
        getOrdersPaginated(rowsPerPage, page, filters)
            .then(data => {
                setIsLoading(false);
                setPagination(data as Pagination);
            })
            .catch(error => {
                setIsLoading(false);
                console.error("Erro ao buscar pedidos:", error);
            });
    }, [rowsPerPage, page, filters]);

    useEffect(() => {
        loadTable();
    }, [page, rowsPerPage]);

    useEffect(() => {
        setRowsPerPage(10)
    }, []);


    const handleSearch = () => {
        setPage(0);
        loadTable();
    };

    const handleCancelOnClick = (order: OrderListingProjection) => {
        if (order.status === 'CANCELLED') {
            alert(`Pedido '${order.externalReference}' já está cancelado`);
            return;
        }
        if (confirm(`Deseja cancelar o pedido '${order.externalReference}'?`)) {
            cancelOrder(order.externalReference)
                .then(() => {
                    alert("Cancelado com sucesso");
                    loadTable();
                })
                .catch(() => alert("Erro ao cancelar"));
        }
    };

    if (isLoading) {
        return (
            <Backdrop sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }} open={true}>
                <CircularProgress color="inherit" size={60} />
                <Typography variant="h6" sx={{ ml: 2 }}>Carregando...</Typography>
            </Backdrop>
        );
    }

    return (
        <Box sx={{
            width: '100vw',        // Ocupa a largura da viewport
            maxWidth: '100%',      // Previne overflow lateral
            p: { xs: 1, sm: 3 },
            boxSizing: 'border-box',
            overflowX: 'hidden'    // Garante que nada "vaze" para os lados
        }}>
            <Paper elevation={2} sx={{ p: 2, mb: 3, borderRadius: 2, width: '100%', boxSizing: 'border-box' }}>
                <Grid container spacing={2} sx={{ width: '100%', m: 0 }}>
                    <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                        <TextField
                            fullWidth
                            select
                            label="Status"
                            value={filters.status}
                            onChange={handleFilterChange('status')}
                            size="small"
                            slotProps={{
                                select: {
                                    MenuProps: {
                                        PaperProps: {
                                            style: { maxWidth: 250 }
                                        }
                                    }
                                }
                            }}
                        >
                            <MenuItem value="ALL">Todos</MenuItem>
                            <MenuItem value="COMPLETED">Completado</MenuItem>
                            <MenuItem value="PENDING">Pendente</MenuItem>
                            <MenuItem value="CANCELLED">Cancelado</MenuItem>
                        </TextField>
                    </Grid>

                    {/* ... Repita os outros Grids mantendo o padrão de 'size' ... */}

                    <Grid size={{ xs: 12, sm: 6, md: 3 }}>
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

                    <Grid size={{ xs: 6, sm: 6, md: 2 }}>
                        <TextField
                            fullWidth label="Início" type="date"
                            value={filters.startDate ? filters.startDate.toString().substring(0, 10) : ''}
                            onChange={handleFilterChange('startDate')}
                            size="small"
                            slotProps={{ inputLabel: { shrink: true } }}
                        />
                    </Grid>

                    <Grid size={{ xs: 6, sm: 6, md: 2 }}>
                        <TextField
                            fullWidth label="Fim" type="date"
                            value={filters.endDate ? filters.endDate.toString().substring(0, 10) : ''}
                            onChange={handleFilterChange('endDate')}
                            size="small"
                            slotProps={{ inputLabel: { shrink: true } }}
                        />
                    </Grid>

                    <Grid size={{ xs: 12, md: 2 }}>
                        <Stack direction="row" spacing={1} sx={{ width: '100%' }}>
                            <Button
                                fullWidth variant="contained" onClick={handleSearch}
                                startIcon={<FilterList />}
                            >
                                Filtrar
                            </Button>
                            <Button
                                variant="outlined" color="inherit" onClick={handleClear}
                                sx={{ minWidth: '48px' }}
                            >
                                <Clear fontSize="small" />
                            </Button>
                        </Stack>
                    </Grid>
                </Grid>
            </Paper>


            {isMobile ? (
                /* --- VISÃO MOBILE (CARDS) --- */
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    {pagination.content.map((order) => (
                        <Card key={order.orderId} elevation={3} sx={{ borderRadius: 2 }}>
                            <CardContent>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                    <Typography variant="subtitle1" fontWeight="bold">
                                        {order.externalReference}
                                    </Typography>
                                    <Chip
                                        label={order.status}
                                        size="small"
                                        color={order.status === 'CANCELLED' ? 'error' : 'success'}
                                        sx={{ fontWeight: 'bold' }}
                                    />
                                </Box>

                                <Typography variant="body2" color="text.secondary">
                                    <strong>Comprador:</strong> {order.buyerName}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    <strong>Data:</strong> {formatDate(order.createdAt)}
                                </Typography>

                                <Divider sx={{ my: 1.5 }} />

                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <Typography variant="subtitle1" fontWeight="bold" color="primary">
                                        {formatCurrency(order.total)}
                                    </Typography>
                                    <Stack direction="row" spacing={1}>
                                        <IconButton size="small" color="primary" onClick={() => navigate(`/orders/details/${order.externalReference}`)}>
                                            <Visibility fontSize="small" />
                                        </IconButton>
                                        <IconButton size="small" color="error" onClick={() => handleCancelOnClick(order)}>
                                            <Cancel fontSize="small" />
                                        </IconButton>
                                    </Stack>
                                </Box>
                            </CardContent>
                        </Card>
                    ))}
                </Box>
            ) : (
                /* --- VISÃO DESKTOP (TABELA) --- */
                <TableContainer component={Paper} elevation={4} sx={{ borderRadius: 2 }}>
                    <Table sx={{ minWidth: 800 }} size="small">
                        <TableHead>
                            <TableRow sx={{ backgroundColor: 'action.selected' }}>
                                <TableCell>Referência</TableCell>
                                <TableCell>Data</TableCell>
                                <TableCell>Comprador</TableCell>
                                <TableCell>Status</TableCell>
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
                                    <TableCell>
                                        <Chip
                                            label={order.status}
                                            size="small"
                                            color={order.status === 'CANCELLED' ? 'error' : 'success'}
                                            sx={{ fontWeight: 'bold' }}
                                        />
                                    </TableCell>
                                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>
                                        {formatCurrency(order.total)}
                                    </TableCell>
                                    <TableCell align="center">
                                        <Stack direction="row" spacing={1} sx={{ justifyContent: 'center' }}>
                                            <IconButton size="small" color="primary" onClick={() => navigate(`/orders/details/${order.externalReference}`)}>
                                                <Visibility fontSize="small" />
                                            </IconButton>
                                            <IconButton size="small" color="error" onClick={() => handleCancelOnClick(order)}>
                                                <Cancel fontSize="small" />
                                            </IconButton>
                                        </Stack>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </TableContainer>
            )}
            <TablePagination
                component="div"
                count={pagination.totalElements}
                rowsPerPage={rowsPerPage}
                page={page}
                onPageChange={handleChangePage}
                onRowsPerPageChange={handleChangeRowsPerPage}
                labelRowsPerPage={isMobile ? "" : "Linhas:"}
            />


        </Box >
    );
};

export default Orders;