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
    Backdrop,
    CircularProgress,
    useMediaQuery,
    useTheme
} from '@mui/material';
import { Visibility, Cancel, FilterList } from '@mui/icons-material';

import { cancelOrder, getOrdersPaginated } from '../../services/order-service';
import type OrderFilters from '../../models/order-filters';
import { formatISO, isValid } from 'date-fns';
import type Pagination from '../../models/pagination';
import type OrderListingProjection from '../../models/order-listing-projection';
import { useNavigate } from 'react-router-dom';
import type { AxiosError } from 'axios';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ptBR } from 'date-fns/locale';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';

const Orders: React.FC = () => {
    const navigate = useNavigate();
    const theme = useTheme();

    // Detecta se a tela é menor que 600px (celular)
    const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

    const [page, setPage] = useState<number>(0);
    const [rowsPerPage, setRowsPerPage] = useState<number>(10);
    const [dateFrom, setDateFrom] = useState<Date | null>(new Date(Date.now() - 7 * 24 * 60 * 60 * 1000));
    const [dateTo, setDateTo] = useState<Date | null>(new Date());
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

    useEffect(() => {
        setFilters((prev) => ({ ...prev, startDate: dateFrom ? dateFrom : new Date(), endDate: dateTo ? dateTo : new Date() }));
    }, [dateFrom, dateTo]);

    const formatCurrency = (value: number) =>
        new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);

    const formatDate = (dateString: string) =>
        new Date(dateString).toLocaleDateString('pt-BR');

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



    const loadTable = useCallback(() => {
        setIsLoading(true);
        getOrdersPaginated(rowsPerPage, page, filters)
            .then(data => {
                setIsLoading(false);
                setPagination(data as Pagination);
            })
            .catch((error: AxiosError) => {
                if (error.response && error.response.status === 403) {
                    alert("Sessão expirada, faça login novamente.")
                    navigate("/");
                    return;
                }
                setIsLoading(false);
                console.error("Erro ao buscar pedidos:", error);
            });
    }, [rowsPerPage, page, filters, navigate]);

    useEffect(() => {
        loadTable();
    }, [loadTable]);

    const handleCancelOnClick = (order: OrderListingProjection) => {
        if (order.status === 'CANCELLED') {
            alert(`Pedido '${order.externalReference}' já está cancelado`);
            return;
        }
        if (window.confirm(`Deseja cancelar o pedido '${order.externalReference}'?`)) {
            cancelOrder(order.externalReference)
                .then(() => {
                    alert("Cancelado com sucesso");
                    loadTable();
                })
                .catch((error: AxiosError) => {
                    if (error.response && error.response.status === 403) {
                        alert("Sessão expirada, faça login novamente.")
                        navigate("/");
                    }
                });
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
            flexGrow: 1,
            width: '100%',
            maxWidth: '100%',
            display: 'block',
            p: { xs: 1, sm: 3 },
            boxSizing: 'border-box',
            overflowX: 'hidden'
        }}>
            {/* --- FILTROS --- */}
            <Paper elevation={0} sx={{ p: 2, mb: 3, borderRadius: 2, border: '1px solid rgba(255,255,255,0.1)', backgroundColor: 'background.paper' }}>
                <Grid container spacing={2}>
                    <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                        <TextField
                            fullWidth select label="Status"
                            value={filters.status || "ALL"}
                            onChange={handleFilterChange('status')}
                            size="small"
                        >
                            <MenuItem value="ALL">Todos</MenuItem>
                            <MenuItem value="COMPLETED">Completado</MenuItem>
                            <MenuItem value="PENDING">Pendente</MenuItem>
                            <MenuItem value="CANCELLED">Cancelado</MenuItem>
                        </TextField>
                    </Grid>

                    <Grid size={{ xs: 12, sm: 6, md: 3 }}>
                        <TextField
                            fullWidth label="Ref. Comprador" value={filters.buyerRef}
                            onChange={handleFilterChange('buyerRef')} size="small"
                        />
                    </Grid>

                    <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                        <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ptBR}>
                            <DatePicker
                                label="De"
                                value={dateFrom}
                                onChange={(newValue) => setDateFrom(newValue)}
                                format="dd/MM/yyyy"
                                slotProps={{
                                    textField: {
                                        size: 'small',
                                        sx: {
                                            backgroundColor: 'background.paper',
                                            borderRadius: 1,
                                            '& .MuiInputBase-input': {
                                                color: '#fff',
                                                '&::placeholder': {
                                                    color: '#fff',
                                                    opacity: 1,
                                                },
                                            },
                                            '& .MuiInputLabel-root': {
                                                color: 'rgba(255, 255, 255, 0.7)',
                                                '&.Mui-focused': { color: '#fff' },
                                            },
                                            '& .MuiOutlinedInput-root': {
                                                '& fieldset': {
                                                    borderColor: 'rgba(255, 255, 255, 0.3)',
                                                },
                                                '&:hover fieldset': {
                                                    borderColor: '#fff',
                                                },
                                                '&.Mui-focused fieldset': {
                                                    borderColor: '#3399FF',
                                                },
                                            },
                                            '& .MuiIconButton-root': {
                                                color: '#fff',
                                            },
                                        },
                                    },
                                }}
                            />
                        </LocalizationProvider>
                    </Grid>

                    <Grid size={{ xs: 6, sm: 6, md: 3 }}>
                        <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ptBR}>
                            <DatePicker
                                label="Até"
                                value={dateTo}
                                onChange={(newValue) => setDateTo(newValue)}
                                format="dd/MM/yyyy"
                                slotProps={{
                                    textField: {
                                        size: 'small',
                                        sx: {
                                            backgroundColor: 'background.paper',
                                            borderRadius: 1,
                                            '& .MuiInputBase-input': {
                                                color: '#fff',
                                                '&::placeholder': {
                                                    color: '#fff',
                                                    opacity: 1,
                                                },
                                            },
                                            '& .MuiInputLabel-root': {
                                                color: 'rgba(255, 255, 255, 0.7)',
                                                '&.Mui-focused': { color: '#fff' },
                                            },
                                            '& .MuiOutlinedInput-root': {
                                                '& fieldset': {
                                                    borderColor: 'rgba(255, 255, 255, 0.3)',
                                                },
                                                '&:hover fieldset': {
                                                    borderColor: '#fff',
                                                },
                                                '&.Mui-focused fieldset': {
                                                    borderColor: '#3399FF',
                                                },
                                            },
                                            '& .MuiIconButton-root': {
                                                color: '#fff',
                                            },
                                        },
                                    },
                                }}
                            />
                        </LocalizationProvider>
                    </Grid>
                </Grid>
            </Paper>

            {isMobile ? (
                /* --- VISÃO MOBILE (CARDS) --- */
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                    {pagination.content.map((order) => (
                        <Card key={order.orderId} sx={{ borderRadius: 2, backgroundImage: 'none', border: '1px solid rgba(255,255,255,0.1)' }}>
                            <CardContent>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                    <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }}>{order.externalReference}</Typography>
                                    <Chip label={order.status} size="small" color={order.status === 'CANCELLED' ? 'error' : 'success'} />
                                </Box>
                                <Typography variant="body2" color="text.secondary"><strong>Comprador:</strong> {order.buyerName}</Typography>
                                <Divider sx={{ my: 1.5 }} />
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                    <Typography variant="subtitle1" sx={{ fontWeight: 'bold' }} color="primary">{formatCurrency(order.total)}</Typography>
                                    <Stack direction="row" spacing={1}>
                                        <IconButton size="small" color="primary" onClick={() => navigate(`/orders/details/${order.externalReference}`)}><Visibility fontSize="small" /></IconButton>
                                        <IconButton size="small" color="error" onClick={() => handleCancelOnClick(order)}><Cancel fontSize="small" /></IconButton>
                                    </Stack>
                                </Box>
                            </CardContent>
                        </Card>
                    ))}
                </Box>
            ) : (
                <TableContainer component={Paper} sx={{ borderRadius: 2, width: '100%', overflow: 'hidden', backgroundImage: 'none', border: '1px solid rgba(255,255,255,0.1)' }}>
                    <Table sx={{ tableLayout: 'fixed', width: '100%' }} size="small">
                        <TableHead>
                            <TableRow sx={{ backgroundColor: 'rgba(255,255,255,0.05)' }}>
                                <TableCell sx={{ width: '16%' }}>Referência</TableCell>
                                <TableCell sx={{ width: '15%' }}>Data</TableCell>
                                <TableCell sx={{ width: '15%' }}>Comprador</TableCell>
                                <TableCell sx={{ width: '15%' }}>Vendedor</TableCell>
                                <TableCell sx={{ width: '15%' }}>Galpão</TableCell>
                                <TableCell sx={{ width: '120px' }}>Status</TableCell>
                                <TableCell align="right" sx={{ width: '15%' }}>Total</TableCell>
                                <TableCell align="center" sx={{ width: '100px' }}>Ações</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {pagination.content.map((order) => (
                                <TableRow key={order.orderId} hover>
                                    <TableCell sx={{ fontWeight: 'bold', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {order.externalReference}
                                    </TableCell>
                                    <TableCell sx={{ whiteSpace: 'nowrap' }}>{formatDate(order.createdAt)}</TableCell>
                                    <TableCell sx={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {order.buyerName}
                                    </TableCell>
                                    <TableCell sx={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {order.sellerName}
                                    </TableCell>
                                    <TableCell sx={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                        {order.warehouseName}
                                    </TableCell>
                                    <TableCell>
                                        <Chip label={order.status} size="small" color={order.status === 'CANCELLED' ? 'error' : 'success'} sx={{ fontSize: '0.7rem' }} />
                                    </TableCell>
                                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>{formatCurrency(order.total)}</TableCell>
                                    <TableCell align="center">
                                        <Stack direction="row" spacing={0.5} sx={{ justifyContent: 'center' }}>
                                            <IconButton size="small" color="primary" onClick={() => navigate(`/orders/details/${order.externalReference}`)}><Visibility fontSize="small" /></IconButton>
                                            <IconButton size="small" color="error" onClick={() => handleCancelOnClick(order)}><Cancel fontSize="small" /></IconButton>
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
                sx={{ color: 'text.secondary' }}
            />
        </Box>
    );
};

export default Orders;