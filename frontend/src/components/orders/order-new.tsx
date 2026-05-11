import React, { useEffect, useState } from 'react';
import {
  Box, Paper, Typography, TextField, Button, IconButton, Stack,
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  InputAdornment, GlobalStyles, Autocomplete, useTheme, useMediaQuery, Grid,
  Backdrop,
  CircularProgress
} from '@mui/material';
import { Add, Delete, Save, ArrowBack, Assignment, Inventory, ShoppingCart, CreditCard } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

// Tipagens (Mantenha seus imports originais)
import type { Order } from '../../models/order';
import type { OrderItem } from '../../models/order-item';
import type { Buyer } from '../../models/buyer';
import { getBuyers } from '../../services/buyer-service';
import { getSellers } from '../../services/seller-service';
import type { Seller } from '../../models/seller';
import { getWarehousesBySellerExternalReference } from '../../services/warehouse-service';
import type { Warehouse } from '../../models/warehouse';
import { getPaymentConditions } from '../../services/payment-condition-service';
import type { PaymentCondition } from '../../models/payment-condition';
import { getProductPrices } from '../../services/product-price-service';
import type { ProductPrice } from '../../models/product-price';
import { createOrder } from '../../services/order-service';
import type { ErrorResponse } from '../../models/error';

const CreateOrder: React.FC = () => {
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const [order, setOrder] = useState<Order>({
    externalReference: '',
    buyerReference: '',
    sellerReference: '',
    warehouseReference: '',
    paymentConditionCode: '',
    items: []
  });

  const [tempItem, setTempItem] = useState<OrderItem>({ productName: '', productCode: '', quantity: 1 });
  const [buyers, setBuyers] = useState<Buyer[]>([]);
  const [sellers, setSellers] = useState<Seller[]>([]);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [productPrices, setProductPrices] = useState<ProductPrice[]>([]);
  const [paymentConditions, setPaymentConditions] = useState<PaymentCondition[]>([]);
  const [selectedSellerExternalReference, setSelectedSellerExternalReference] = useState<string | null>(null);
  const [selectedWarehouseExternalReference, setSelectedWarehouseExternalReference] = useState<string | null>(null);

  const absoluteLock = {
    '& .MuiOutlinedInput-root': {
      height: '50px',
      borderRadius: '12px',
      backgroundColor: 'rgba(255, 255, 255, 0.02)',
      '& fieldset': { borderColor: 'rgba(255, 255, 255, 0.1)', borderWidth: '1px !important' },
      '&.Mui-focused fieldset': { borderColor: '#3399FF', borderWidth: '1px !important' },
      padding: '0 14px !important',
    },
    '& .MuiInputLabel-root': {
      transform: 'translate(14px, 14px) scale(1)',
      '&.MuiInputLabel-shrink': { transform: 'translate(14px, -9px) scale(0.75)' }
    }
  };

  useEffect(() => {
    getBuyers()
      .then(setBuyers)
      .catch(err => {
        console.error(err)
        if (err.response.status === 403) {
          alert("Sessão expirada, faça login novamente.")
          navigate('/');
        }
      })
    getSellers()
      .then(setSellers)
      .catch(err => {
        console.error(err)
        if (err.response.status === 403) {
          navigate('/');
          alert("Sessão expirada, faça login novamente.")
        }
      });
    getPaymentConditions()
      .then(setPaymentConditions)
      .catch(err => {
        console.error(err)
        if (err.response.status === 403) {
          alert("Sessão expirada, faça login novamente.")
          navigate('/');
        }
      });
  }, []);

  useEffect(() => {
    if (selectedSellerExternalReference) {
      getWarehousesBySellerExternalReference(selectedSellerExternalReference)
        .then(setWarehouses)
        .catch(err => console.error(err));
    } else {
      setWarehouses([]);
    }
  }, [selectedSellerExternalReference]);

  useEffect(() => {
    if (selectedWarehouseExternalReference) {
      getProductPrices(selectedWarehouseExternalReference)
        .then(setProductPrices)
        .catch(err => console.error(err));
    } else {
      setProductPrices([]);
    }
  }, [selectedWarehouseExternalReference]);

  const handleAddItem = () => {
    if (!tempItem.productCode) {
      alert("Selecione um item na lista para adicionar.")
      return;
    }
    setOrder(prev => ({ ...prev, items: [...prev.items, tempItem] }));
    setTempItem({ productName: '', productCode: '', quantity: 1 });
  };

  const removeItem = (index: number) => {
    setOrder(prev => ({ ...prev, items: prev.items.filter((_, i) => i !== index) }));
  };

  const validateOrder = (): boolean => {
    if (!order.externalReference.trim()) {
      alert("Informe a Referência do Pedido.");
      return false;
    }
    if (!order.buyerReference) {
      alert("Selecione um Comprador.");
      return false;
    }
    if (!order.sellerReference) {
      alert("Selecione um Vendedor.");
      return false;
    }
    if (!order.warehouseReference) {
      alert("Selecione um Galpão / Depósito.");
      return false;
    }
    if (!order.paymentConditionCode) {
      alert("Selecione a Forma de Pagamento.");
      return false;
    }
    if (order.items.length === 0) {
      alert("Adicione pelo menos um produto ao pedido.");
      return false;
    }
    return true;
  };

  const handleCreateOrder = () => {
    if(!validateOrder()) return;
    createOrder(order)
      .then(result => {
        if (result) {
          alert("Pedido criado com sucesso!");
          navigate("/orders");
        } else {
          alert("Erro ao criar pedido.");
        }
      }).catch(err => {
        console.error(err);
        if (err.response.status === 403) {
          alert("Sessão expirada, faça login novamente.")
          navigate('/');
          return;
        }
        const data = err.response.data as ErrorResponse;
        alert(`
            Erro ao criar o pedido.
            detlhes do erro:
            ${data.message}
          `);
      });
  }

  if (buyers.length === 0 || sellers.length === 0  || paymentConditions.length === 0 ){
        return (
            <Backdrop sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }} open={true}>
                <CircularProgress color="inherit" size={60} />
                <Typography variant="h6" sx={{ ml: 2 }}>Carregando...</Typography>
            </Backdrop>
        );
  }

  return (
    <Box sx={{ p: { xs: 2, sm: 4 }, maxWidth: '1200px', margin: '0 auto' }}>
      <GlobalStyles styles={{ body: { paddingRight: '0px !important', overflowY: 'scroll !important' } }} />

      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
          <IconButton onClick={() => navigate(-1)} sx={{ bgcolor: 'rgba(255,255,255,0.05)', borderRadius: '12px' }}>
            <ArrowBack color="primary" />
          </IconButton>
          <Typography variant="h5" sx={{ fontWeight: 800 }}>Novo Pedido</Typography>
        </Stack>
        <Button onClick={() => handleCreateOrder()} variant="contained" startIcon={<Save />} sx={{ borderRadius: '12px', px: 4, height: '50px', fontWeight: 'bold', color: '#FFF' }}>
          Salvar
        </Button>
      </Box>

      <Stack spacing={4}>
        {/* Identificação */}
        <Paper elevation={0} sx={{ p: 4, borderRadius: '24px', border: '1px solid rgba(255,255,255,0.1)', bgcolor: 'background.paper' }}>
          <Typography variant="subtitle2" sx={{ mb: 3, fontWeight: 'bold', color: 'primary.main', display: 'flex', alignItems: 'center', gap: 1 }}>
            <Assignment fontSize="small" /> DADOS DE IDENTIFICAÇÃO
          </Typography>

          <Grid container spacing={3}>
            <Grid size={12}>
              <TextField
                label="Ref. Pedido"
                value={order.externalReference}
                onChange={(e) => setOrder({ ...order, externalReference: e.target.value })}
                fullWidth
                sx={absoluteLock}
                slotProps={{ input: { startAdornment: <InputAdornment position="start"><Assignment fontSize="small" color="primary" /></InputAdornment> } }}
              />
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <Autocomplete
                options={buyers.map(b => ({ label: b.name, value: b.externalReference }))}
                value={buyers.map(b => ({ label: b.name, value: b.externalReference })).find(opt => opt.value === order.buyerReference) || null}
                onChange={(_, newValue) => setOrder(prev => ({ ...prev, buyerReference: newValue?.value || '' }))}
                sx={absoluteLock}
                renderInput={(params) => <TextField {...params} label="Comprador" />}
              />
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <Autocomplete
                options={sellers.map(s => ({ label: s.name, value: s.externalReference }))}
                value={sellers.map(s => ({ label: s.name, value: s.externalReference })).find(opt => opt.value === order.sellerReference) || null}
                onChange={(_, newValue) => {
                  const val = newValue?.value || '';
                  setSelectedSellerExternalReference(val);
                  setOrder(prev => ({ ...prev, sellerReference: val, warehouseReference: '' }));
                }}
                sx={absoluteLock}
                renderInput={(params) => <TextField {...params} label="Vendedor" />}
              />
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <Autocomplete
                key={`warehouse-${selectedSellerExternalReference}`}
                options={warehouses.map(w => ({ label: w.name, value: w.externalReference }))}
                value={warehouses.map(w => ({ label: w.name, value: w.externalReference })).find(opt => opt.value === order.warehouseReference) || null}
                onChange={(_, newValue) => {
                  const val = newValue?.value || '';
                  setSelectedWarehouseExternalReference(val);
                  setOrder(prev => ({ ...prev, warehouseReference: val, items: [] }));
                }}
                sx={absoluteLock}
                renderInput={(params) => <TextField {...params} label="Galpão / Depósito" />}
              />
            </Grid>

            <Grid size={{ xs: 12, md: 6 }} >
              <Autocomplete
                options={paymentConditions.map(pc => ({ label: pc.description, value: pc.code }))}
                disablePortal
                onChange={(_, newValue) => setOrder(prev => ({ ...prev, paymentConditionCode: newValue?.value || '' }))}
                getOptionLabel={(option) => option.label || ""}
                sx={absoluteLock}
                renderInput={(params) => <TextField {...params} label="Forma de Pagamento" />}
              />
            </Grid>
          </Grid>
        </Paper>

        {/* Produtos */}
        <Paper elevation={0} sx={{ p: 4, borderRadius: '24px', border: '1px solid rgba(255,255,255,0.1)', bgcolor: 'background.paper' }}>
          <Typography variant="subtitle2" sx={{ mb: 3, fontWeight: 'bold', color: 'primary.main', display: 'flex', alignItems: 'center', gap: 1 }}>
            <Inventory fontSize="small" /> ADICIONAR PRODUTOS
          </Typography>

          <Grid container spacing={2} sx={{ mb: 4, alignItems: 'center' }}>
            <Grid size={{ xs: 12, md: 7 }}>
              <Autocomplete
                options={productPrices.map(pp => ({ label: pp.productName, value: pp.productCode }))}
                value={productPrices.map(pp => ({ label: pp.productName, value: pp.productCode })).find(opt => opt.value === tempItem.productCode) || null}
                onChange={(_, newValue) => setTempItem(prev => ({ ...prev, productCode: newValue?.value || '', productName: newValue?.label || '' }))}
                onInputChange={(_, val) => setTempItem(prev => ({ ...prev, productCode: val }))}
                sx={absoluteLock}
                renderInput={(params) => <TextField {...params} label="Cód. ou Nome do Produto" />}
              />
            </Grid>
            <Grid size={{ xs: 8, md: 3 }}>
              <TextField
                fullWidth
                type="number"
                label="Quantidade"
                value={tempItem.quantity}
                onChange={(e) => setTempItem(prev => ({ ...prev, quantity: Number(e.target.value) }))}
                slotProps={{
                  htmlInput: {
                    min: 1,
                    step: 1
                  }
                }}
                sx={absoluteLock}
              />
            </Grid>
            <Grid size={{ xs: 4, md: 2 }}>
              <Button
                fullWidth
                variant="contained"
                onClick={handleAddItem}
                startIcon={<Add />}
                sx={{ height: '50px', borderRadius: '12px', fontWeight: 'bold', color: '#FFF' }}
              >
                {isMobile ? '' : 'Add'}
              </Button>
            </Grid>
          </Grid>

          <TableContainer sx={{ borderRadius: '16px', border: '1px solid rgba(255,255,255,0.05)', maxHeight: 400 }}>
            <Table stickyHeader size={isMobile ? "small" : "medium"}>
              <TableHead>
                <TableRow>
                  <TableCell sx={{ bgcolor: 'rgba(255,255,255,0.02)', fontWeight: 'bold' }}>Produto</TableCell>
                  <TableCell align="center" sx={{ bgcolor: 'rgba(255,255,255,0.02)', fontWeight: 'bold' }}>Qtd</TableCell>
                  <TableCell align="right" sx={{ bgcolor: 'rgba(255,255,255,0.02)', fontWeight: 'bold' }}>Ações</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {order.items.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={3} align="center" sx={{ py: 4, color: 'text.secondary' }}>Nenhum produto adicionado.</TableCell>
                  </TableRow>
                ) : (
                  order.items.map((item, index) => (
                    <TableRow key={index} hover>
                      <TableCell sx={{ fontWeight: 500 }}>
                        <Stack direction="row" spacing={1} sx={{ alignItems: 'center' }}>
                          <ShoppingCart fontSize="small" sx={{ opacity: 0.5 }} />
                          {item.productName}
                        </Stack>
                      </TableCell>
                      <TableCell align="center">{item.quantity}</TableCell>
                      <TableCell align="right">
                        <IconButton color="error" onClick={() => removeItem(index)}>
                          <Delete fontSize="small" />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      </Stack>
    </Box>
  );
};

export default CreateOrder;