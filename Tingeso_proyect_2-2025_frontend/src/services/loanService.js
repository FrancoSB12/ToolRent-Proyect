import httpClient from './http-common';

const create = data => {
  return httpClient.post('/api/loans', data);
}

const getAll = () => {
  return httpClient.get('/api/loans/');
}

const getById = id => {
  return httpClient.get(`/api/loans/${id}`);
}

const getActiveByClientRun = run => {
  return httpClient.get(`/api/loans/client/${run}`);
}

const getByStatus = status => {
  return httpClient.get(`/api/loans/status/${status}`);
}

const getByValidity = validity => {
  return httpClient.get(`/api/loans/validity/${validity}`);
}

const getMostLoanedTools = () => {
  return httpClient.get('/api/loans/most-loaned-tools');
}

const getCurrentLateFee = () => {
  return httpClient.get('/api/loans/configuration/late-return-fee');
}

const returnLoan = (id, data) => {
  return httpClient.put(`/api/loans/return/${id}`, data);
}

const updateLateReturnFee = (id, data) => {
  return httpClient.put(`/api/loans/update-late-fee/${id}`, data);
}

const updateGlobalLateReturnFee = (amount) => {
  return httpClient.put(`/api/loans/configuration/late-fee?amount=${amount}`, {});
}

const updateLateStatuses = () => {
  return httpClient.post("/api/loans/update-late-statuses");
}

export default { create, getAll, getById, getActiveByClientRun, getByStatus, getByValidity, getMostLoanedTools, getCurrentLateFee, returnLoan, updateLateReturnFee, updateGlobalLateReturnFee, updateLateStatuses };